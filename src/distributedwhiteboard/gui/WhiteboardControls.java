package distributedwhiteboard.gui;

import distributedwhiteboard.Client;
import distributedwhiteboard.DrawMode;
import distributedwhiteboard.WhiteboardMessage;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import say.swing.JFontChooser;

/**
 * Provides a set of controls to handle drawing to a specified {@link 
 * WhiteboardCanvas}. The controls allow the selection of drawing mode, drawing 
 * colour, line width, font and shape border settings.
 *
 * @author 6266215
 * @version 1.2
 * @since 2015-03-15
 */
public final class WhiteboardControls extends JPanel 
        implements ActionListener,  // Listen for button clicks.
                ItemListener,       // Listen for checkbox/ combobox changes.
                KeyListener,        // Listen for keyboard events.
                MouseListener,      // Listen for mouse clicks.
                MouseMotionListener // Listen for mouse movements.
{
    /** Serialisation ID. */
    private static final long serialVersionUID = -6184851091001506327L;
    /** Identifier for the text tools card. */
    private static final String TEXT = "TEXT";
    /** Identifier for the line tools card. */
    private static final String LINES = "LINES";
    /** Identifier for the shape tools card. */
    private static final String SHAPES = "SHAPES";
    /** Identifier for the image tools card. */
    private static final String IMAGES = "IMAGES";
    
    // <editor-fold defaultstate="collapsed" desc="Swing components">
    /** The {@link WhiteboardCanvas} to draw to. */
    private final WhiteboardCanvas canvas;
    // Layout managers for automatically spaced panels.
    private final FlowLayout layout, toolInnerLayout;
    /** A {@link CardLayout} to hide tool panels. */
    private final CardLayout toolLayout;
    // A border for tool panels.
    private final Border toolBorder;
    // Tons of other Swing components.
    private final JPanel toolBox, fontTools, lineTools, shapeTools, imageTools, 
            colPreview, bPreview;
    // Drawing mode selector.
    private final JComboBox<DrawMode> modeSelect;
    // Line weight selection.
    private final JComboBox<Integer> weightSelect, borderWSelect;
    // Checkboxes to enable/ disable features.
    private final JCheckBox setFilled, setBorder;
    // Buttons to launch pickers.
    private final JButton colourPicker, fontPicker, borderPicker, imagePicker;
    // Font control buttons.
    private final JToggleButton boldButton, italicButton, underButton;
    // Labels for unlabeled controls.
    private final JLabel modeLabel, weightLabel, borderWLabel, fontPreview;
    // Icons for the buttons in these controls.
    private final ImageIcon fontIcon, boldIcon, italicIcon, underlineIcon;
    // </editor-fold>
    // Points for drawing lines.
    private Point lastPoint, firstPoint;
    // The current drawing mode.
    private DrawMode mode;
    // Colours for shapes and borders.
    private Color colour, borderColour;
    // The currently selected font.
    private Font font;
    // Line and border weights.
    private int lineWeight, borderWeight;
    // Settings for extra shape drawing features.
    private boolean fillShape, borderShape;
    // Image to draw to the screen.
    private BufferedImage image;
    
    /**
     * Set up a new set of {@link WhiteboardControls} for a specified {@link 
     * WhiteboardCanvas}.
     * 
     * @param canvas The {@link WhiteboardCanvas} to control.
     * @since 1.0
     */
    public WhiteboardControls(WhiteboardCanvas canvas)
    {
        super();
        this.canvas = canvas;
        this.layout = new FlowLayout();
        this.toolLayout = new CardLayout();
        this.toolInnerLayout = new FlowLayout(FlowLayout.CENTER, 5, 2);
        this.toolBorder = new MatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY);
        this.setLayout(layout);
        
        String path = "/distributedwhiteboard/gui/assets/";
        this.fontIcon = WhiteboardGUI.createIcon(
                path+"preferences-desktop-font.png");
        this.boldIcon = WhiteboardGUI.createIcon(
                path+"format-text-bold.png");
        this.italicIcon = WhiteboardGUI.createIcon(
                path+"format-text-italic.png");
        this.underlineIcon = WhiteboardGUI.createIcon(
                path+"format-text-underline.png");
        
        this.modeLabel = new JLabel("Tool:");
        this.modeSelect = new JComboBox<>(DrawMode.values());
        this.modeSelect.setEditable(false);
        
        this.colourPicker = new JButton("Colour");
        
        this.colPreview = new JPanel();
        this.colPreview.setBorder(new LineBorder(Color.BLACK));
        
        this.toolBox = new JPanel(toolLayout);
        
        this.fontTools = new JPanel(toolInnerLayout);
        this.fontTools.setBorder(toolBorder);
        
        this.fontPreview = new JLabel("Text");
        this.fontPreview.setBorder(new EmptyBorder(0, 5, 0, 5));
        
        this.fontPicker = new JButton("Font", fontIcon);
        this.boldButton = new JToggleButton("Bold", boldIcon);
        this.italicButton = new JToggleButton("Italic", italicIcon);
        this.underButton = new JToggleButton("Underline", underlineIcon);
        
        this.lineTools = new JPanel(toolInnerLayout);
        this.lineTools.setBorder(toolBorder);
        
        this.weightLabel = new JLabel("Weight:");
        this.weightSelect = new JComboBox<>(new Integer[]{1, 2, 4, 8, 16});
        this.weightSelect.setEditable(false);
        
        this.shapeTools = new JPanel(toolInnerLayout);
        this.shapeTools.setBorder(toolBorder);
        
        this.setFilled = new JCheckBox("Filled:");
        this.setFilled.setHorizontalTextPosition(SwingConstants.LEFT);
        this.setFilled.setSelected(false);
        
        this.setBorder = new JCheckBox("Border:");
        this.setBorder.setHorizontalTextPosition(SwingConstants.LEFT);  
        this.setBorder.setSelected(false);
        
        this.borderWLabel = new JLabel("Weight:");
        
        this.bPreview = new JPanel();
        this.bPreview.setBorder(new LineBorder(Color.BLACK));
        
        this.borderPicker = new JButton("Border Colour");
        this.borderPicker.setEnabled(false);
        
        this.borderWSelect = new JComboBox<>(new Integer[]{1, 2, 4, 8, 16});
        this.borderWSelect.setEditable(false);
        this.borderWSelect.setEnabled(false);
        
        this.imageTools = new JPanel(toolInnerLayout);
        this.imageTools.setBorder(toolBorder);
        
        this.imagePicker = new JButton("Pick Image");
        
        this.mode = DrawMode.LINE;
        this.font = new Font("Arial", Font.PLAIN, 12);
        this.colour = Color.BLACK;
        this.lineWeight = 1;
        this.lastPoint = null;
        this.firstPoint = null;
        this.fillShape = false;
        this.borderShape = false;
        this.borderWeight = 1;
        this.borderColour = Color.WHITE;
        this.image = null;
        
        setupLayout();
    }
    
    /**
     * Attach the various listeners to the components that need them.
     * 
     * @since 1.0
     */
    private void setupLayout()
    {
        colourPicker.addActionListener(this);
        modeSelect.addItemListener(this);
        
        fontPicker.addActionListener(this);
        boldButton.addActionListener(this);
        italicButton.addActionListener(this);
        underButton.addActionListener(this);
        
        weightSelect.addItemListener(this);
        setFilled.addItemListener(this);
        setBorder.addItemListener(this);
        borderPicker.addActionListener(this);
        borderWSelect.addItemListener(this);
        
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        
        this.add(modeLabel);
        this.add(modeSelect);
        modeSelect.setSelectedItem(mode);
        
        this.add(colourPicker);
        this.add(colPreview);
        
        fontTools.add(fontPreview);
        fontTools.add(fontPicker);
        fontTools.add(boldButton);
        fontTools.add(italicButton);
        fontTools.add(underButton);
        toolBox.add(fontTools, TEXT);
        
        lineTools.add(weightLabel);
        lineTools.add(weightSelect);
        toolBox.add(lineTools, LINES);
        
        shapeTools.add(setFilled);
        shapeTools.add(setBorder);
        shapeTools.add(borderPicker);
        shapeTools.add(bPreview);
        shapeTools.add(borderWLabel);
        shapeTools.add(borderWSelect);
        toolBox.add(shapeTools, SHAPES);
        
        imageTools.add(imagePicker);
        toolBox.add(imageTools, IMAGES);
        
        this.add(toolBox);
        
        disableFontControls();
        colPreview.setBackground(colour);
        bPreview.setBackground(borderColour);
        fontPicker.setFont(font);
        enableDrawControls();
        toolLayout.show(toolBox, LINES);
    }
    
    /**
     * Display the text editing controls.
     * 
     * @since 1.0
     */
    private void enableFontControls()
    {
        Map attribs = font.getAttributes();
        Object underline = attribs.get(TextAttribute.UNDERLINE);
        underButton.setSelected(underline != null);
        boldButton.setSelected(font.isBold());
        italicButton.setSelected(font.isItalic());
        Component[] fontControls = fontTools.getComponents();
        for (Component com : fontControls) {
            com.setEnabled(true);
        }
    }
    
    /**
     * Hide the text editing controls.
     * 
     * @since 1.0
     */
    private void disableFontControls()
    {
        boldButton.setSelected(false);
        italicButton.setSelected(false);
        Component[] fontControls = fontTools.getComponents();
        for (Component com : fontControls) {
            com.setEnabled(false);
        }
    }
    
    /**
     * Enable the shape editing and drawing tools.
     * 
     * @since 1.1
     */
    private void enableDrawControls()
    {
        Component[] drawControls = lineTools.getComponents();
        for (Component com : drawControls) {
            com.setEnabled(true);
        }
        borderPicker.setEnabled(borderShape);
        borderWSelect.setEnabled(borderShape);
        borderWLabel.setEnabled(borderShape);
    }
    
    /**
     * Disable the shape editing and drawing tools.
     * 
     * @since 1.0
     */
    private void disableDrawControls()
    {
        Component[] drawControls = lineTools.getComponents();
        for (Component com : drawControls) {
            com.setEnabled(false);
        }
    }
    
    /**
     * Draw a line from a stored point to the next point on the referenced 
     * {@link WhiteboardCanvas}. If there is no stored point, nextPoint will be 
     * stored until the next invocation of this method.
     * 
     * @param nextPoint The {@link Point} to draw a line to. If no point is 
     * currently stored, this point will be stored.
     * @since 1.0
     */
    private void drawLine(Point nextPoint)
    {
        if (firstPoint == null) // Store the first point passed if none is held.
            firstPoint = nextPoint;
        if (lastPoint == null) {
            lastPoint = nextPoint;
            return;
        }
        
        WhiteboardMessage msg = new WhiteboardMessage(lastPoint, nextPoint, 
                colour, lineWeight);
        lastPoint = canvas.drawLine(lastPoint, nextPoint, colour, lineWeight);
        Client.getInstance().broadCastMessage(msg);
    }
    
    /**
     * Draws a rectangle to the referenced {@link WhiteboardCanvas}. The first 
     * invocation of this will store the origin of a rectangle, and the next 
     * invocation will calculate the width and height of the rectangle to draw.
     * 
     * @param point The {@link Point} to act as the origin and target size of 
     * the rectangle to draw.
     * @since 1.1
     */
    private void drawRect(Point point)
    {
        if (firstPoint == null) {
            firstPoint = point;
        } else {
            // Create the message, we're going to modify some of this data.
            WhiteboardMessage msg = new WhiteboardMessage(firstPoint, point, 
                    colour, borderWeight, fillShape, borderShape, borderColour);
            // Calculate the rectangle size.
            Dimension rectSize = new Dimension(
                    point.x - firstPoint.x, 
                    point.y - firstPoint.y
            );
            
            // Adjust the origin point to allow for negative widths and heights.
            if (rectSize.width < 0 && rectSize.height > 0) { 
                firstPoint = new Point(point.x, firstPoint.y);
            } else if (rectSize.width > 0 && rectSize.height < 0) { 
                firstPoint = new Point(firstPoint.x, point.y);
            } else if (rectSize.width < 0 && rectSize.height < 0) { 
                firstPoint = new Point(point.x, point.y);
            }            
            
            // Draw the rectangle.
            lastPoint = canvas.drawRectangle(firstPoint, rectSize, colour, 
                    fillShape, borderShape, borderWeight, borderColour);
            firstPoint = null; // Reset the origin point.
            Client.getInstance().broadCastMessage(msg);
        }
    }
    
    /**
     * Draw a {@link String} to the referenced {@link WhiteboardCanvas}. The 
     * drawing point for this text is set by clicking the canvas before typing.
     * 
     * @param c The character to draw to the canvas.
     * @since 1.0
     */
    private void drawText(char c)
    {
        if (lastPoint != null) {
            WhiteboardMessage msg = new WhiteboardMessage(lastPoint, colour, 
                    font, c);
            lastPoint = canvas.drawText(c, lastPoint, font, colour);
            Client.getInstance().broadCastMessage(msg);
        }
    }
        
    /**
     * Displays the font choosing dialog. Sets the font to use when drawing 
     * text to the canvas.
     * 
     * @return Returns the new {@link Font} is one is chosen, otherwise returns 
     * the current {@link Font}.
     * @since 1.0
     */
    private Font showFontChooser()
    {
        Font newFont = null;
        JFontChooser fontChooser = new JFontChooser();
        fontChooser.setSelectedFont(font);
        int result = fontChooser.showDialog(this);
		if (result == JFontChooser.OK_OPTION)
		{
			newFont = fontChooser.getSelectedFont(); 
			System.out.println("Selected Font : " + font); 
		}
		if (newFont != null) {
            boldButton.setSelected(newFont.isBold());
            italicButton.setSelected(newFont.isItalic());
            Map attribs = newFont.getAttributes();
            Object underline = attribs.get(TextAttribute.UNDERLINE);
            underButton.setSelected(underline != null);
            fontPreview.setFont(newFont);
            return newFont;
        }
        
        return font;
    }
    
    /**
     * Shows the colour picker dialog. If no new colour is picked, the existing 
     * colour will be returned instead.
     * 
     * @return Returns the new {@link Color} if one is chosen, otherwise returns 
     *  the existing {@link Color}.
     * @since 1.0
     */
    private Color showColourPicker()
    {
        Color newCol = JColorChooser.showDialog(this, "Choose Colour", colour);
        if (newCol != null)
            return newCol;
        
        return colour;
    }
    
    /**
     * Shows the file browser, and allows the user to select an image from their
     *  storage.
     * 
     * @return Returns the selected image as a {@link BufferedImage}. If no 
     * image is selected or an unsupported type is selected, returns null 
     * instead.
     * @since 1.2
     */
    private BufferedImage showImagePicker()
    {
        return null;
    }
    
    /**
     * Toggles the state of the current {@link Font} style for bold. If both 
     * bold and italics are enabled this will set the style appropriately.
     * 
     * @since 1.0
     */
    private void toggleBold()
    {
        if (boldButton.isSelected()) {
            if (italicButton.isSelected()) 
                font = font.deriveFont(Font.ITALIC|Font.BOLD);
            else
                font = font.deriveFont(Font.BOLD);
        } else {
            // Font isn't currently bolded. Set italic style as needed.
            if (italicButton.isSelected())
                font = font.deriveFont(Font.ITALIC);
            else
                font = font.deriveFont(Font.PLAIN);
        }
        fontPreview.setFont(font);
    }
    
    /**
     * Toggles the state of the current {@link Font} style for italics. If both 
     * bold and italics are enabled this will set the style appropriately.
     * 
     * @since 1.0
     */
    private void toggleItalics()
    {
        if (italicButton.isSelected()) {
            if (boldButton.isSelected()) 
                font = font.deriveFont(Font.ITALIC|Font.BOLD);
            else
                font = font.deriveFont(Font.ITALIC);
        } else {
            // Font isn't currently italic. Set bold style as needed.
            if (boldButton.isSelected())
                font = font.deriveFont(Font.BOLD);
            else
                font = font.deriveFont(Font.PLAIN);
        }
        fontPreview.setFont(font);
    }
    
    private void toggleUnderline()
    {
        Map attribs = font.getAttributes();
        Object underline = attribs.get(TextAttribute.UNDERLINE);
        if (underline != null) {
            int underAttr = (int)underline;
            if (underAttr == TextAttribute.UNDERLINE_ON) {
                attribs.put(TextAttribute.UNDERLINE, null);
            }
        } else {
            attribs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        }
        font = font.deriveFont(attribs);
        fontPreview.setFont(font);
    }

    /**
     * Handles various button presses.
     * 
     * @param e The {@link ActionEvent} sent by any button that has been 
     * interacted with.
     * @since 1.0
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        if (source == colourPicker) {
            colour = showColourPicker();
            colPreview.setBackground(colour);
        } else if (source == borderPicker) {
            borderColour = showColourPicker();
            bPreview.setBackground(borderColour);
        } else if (source == fontPicker)
            font = showFontChooser();
        else if (source == boldButton)
            toggleBold();
        else if (source == italicButton)
            toggleItalics();
        else if (source == underButton)
            toggleUnderline();
        else
            System.err.printf("Unknown action! (%s)%n", e.paramString());
    }
    
    /**
     * Sets the currently active drawing mode based on the value selected in the
     *  "Drawing Mode" combo box.
     * 
     * @param e The {@link ItemEvent} sent by the mode selection combo box.
     * @since 1.0
     */
    private void modeSelectAction(ItemEvent e)
    {
        mode = (DrawMode)e.getItem();
        lastPoint = null;
        firstPoint = null;
        switch (mode) {
            case LINE:
            case POLYGON:
            case FREEFORM_LINE:
                enableDrawControls();
                disableFontControls();
                toolLayout.show(toolBox, LINES);
                break;
            case RECTANGLE:
                enableDrawControls();
                disableFontControls();
                toolLayout.show(toolBox, SHAPES);
                break;
            case TEXT:
                enableFontControls();
                disableDrawControls();
                toolLayout.show(toolBox, TEXT);
                break;
            default:
                System.err.println("Unknown drawing mode!");
        }
    }
    
    /**
     * Changes the size of something based on the value selected in a combo box.
     * 
     * @param e The {@link ItemEvent} sent by the combo box.
     * @return Returns an int containing the number selected in the combo box.
     * @since 1.0
     */
    private int sizeSelectAction(ItemEvent e)
    {
        Integer newSize = (Integer)e.getItem();
        return newSize;
    }
    
    /**
     * Handles check boxes being toggled on or off.
     * 
     * @param e The {@link ItemEvent} sent by the changed check box.
     * @return Returns true if the check box has been ticked, false otherwise.
     * @since 1.0
     */
    private boolean checkBoxAction(ItemEvent e)
    {
        Boolean toggle = ((JCheckBox)e.getItem()).isSelected();
        return toggle;
    }

    /**
     * Handles events sent by combo boxes and check boxes.
     * 
     * @param e The {@link ItemEvent} sent from either a combo box or check box.
     * @since 1.0
     */
    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getSource() == modeSelect)
            modeSelectAction(e);
        else if (e.getSource() == weightSelect) {
            lineWeight = sizeSelectAction(e);
        } else if (e.getSource() == borderWSelect) {
            borderWeight = sizeSelectAction(e);
        }else if (e.getSource() == setFilled) {
            fillShape = checkBoxAction(e);
        } else if (e.getSource() == setBorder) {
            borderShape = checkBoxAction(e);
            borderWSelect.setEnabled(borderShape);
            borderPicker.setEnabled(borderShape);
            borderWLabel.setEnabled(borderShape);
        }
    }

    /**
     * Draws the character received in the {@link KeyEvent} to the canvas.
     * 
     * @param e The {@link KeyEvent} sent by the canvas.
     * @since 1.0
     */
    @Override
    public void keyTyped(KeyEvent e)
    {
        if (mode.equals(DrawMode.TEXT)) {
            drawText(e.getKeyChar());
        }
    }

    /**
     * Sets the starting and next points for the various drawing tools, or the 
     * origin for a text drawing.
     * 
     * @param e The {@link MouseEvent} sent by the canvas, we're only interested
     *  in mouse clicks for certain drawing modes.
     * @since 1.0
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        canvas.requestFocusInWindow();
        Point newPoint = e.getPoint();
        
        switch (mode) {
            case LINE:
            case POLYGON:
                switch(e.getButton()) {
                    case MouseEvent.BUTTON1: // Set next point.
                        drawLine(newPoint);
                        break;
                    case MouseEvent.BUTTON3: // Cancel drawing/ finish polygon.
                        if (mode == DrawMode.POLYGON) {
                            drawLine(firstPoint);
                        }
                        lastPoint = null;
                        firstPoint = null;
                        break;
                }
                break;
            case FREEFORM_LINE:
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastPoint = newPoint;
                    drawLine(newPoint);
                    lastPoint = null;
                }
                break;
            case RECTANGLE:
                switch(e.getButton()) {
                    case MouseEvent.BUTTON1: // Set origin/ end point.
                        drawRect(newPoint);
                        break;
                    case MouseEvent.BUTTON3: // Cancel drawing.
                        lastPoint = null;
                        firstPoint = null;
                        break;
                }
                break;
            case TEXT:
                lastPoint = newPoint;
                break;
            default:
                System.err.printf("Drawing mode '%s' not implemented.\n", mode);
        }
    }

    /**
     * If drawing a {@link DrawMode#FREEFORM_LINE}, this will cancel the line 
     * drawing and clear the last stored {@link Point} to prevent extra lines 
     * being drawn.
     * 
     * @param e The {@link MouseEvent} sent by the canvas.
     * @since 1.1
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (mode == DrawMode.FREEFORM_LINE)
            lastPoint = null;
    }

    /**
     * If drawing a {@link DrawMode#FREEFORM_LINE}, this will continuously draw 
     * line segments as the mouse moves with {@link MouseEvent#BUTTON1} held 
     * down.
     * 
     * @param e The {@link MouseEvent} sent by the canvas.
     * @since 1.1
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        switch(mode) {
            case FREEFORM_LINE:
                Point nextPoint = e.getPoint();
                if (!nextPoint.equals(lastPoint))
                    drawLine(e.getPoint());
                break;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Unimplemented handlers.">
    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
    
    @Override
    public void mouseMoved(MouseEvent e) { }
    // </editor-fold>
}
