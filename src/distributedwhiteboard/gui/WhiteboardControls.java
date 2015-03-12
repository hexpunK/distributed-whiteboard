package distributedwhiteboard.gui;

import distributedwhiteboard.DrawMode;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import say.swing.JFontChooser;

/**
 * Provides a set of controls to handle drawing to a specified {@link 
 * WhiteboardCanvas}. The controls allow the selection of drawing mode, drawing 
 * colour, line width, font and shape border settings.
 *
 * @author 6266215
 * @version 1.1
 * @since 2015-03-11
 */
public class WhiteboardControls extends JPanel 
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
    /** Identifier for the shape tools card. */
    private static final String DRAWING = "SHAPES";
    
    // <editor-fold defaultstate="collapsed" desc="Swing components">
    /** The {@link WhiteboardCanvas} to draw to. */
    private final WhiteboardCanvas canvas;
    /** The layout for the various tool bars. */
    private final FlowLayout layout;
    /** A {@link CardLayout} to hide tool panels. */
    private final CardLayout toolLayout;
    // Tons of other Swing components.
    private final JPanel toolBox, fontTools, drawTools;
    private final JComboBox<DrawMode> modeSelect;
    private final JComboBox<Integer> weightSelect, borderWSelect;
    private final JCheckBox setFilled, setBorder;
    private final JButton colourPicker, fontPicker, borderPicker;
    private final JToggleButton boldButton, italicButton;
    private final JLabel modeLabel, weightLabel, borderWLabel;
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
        this.setLayout(layout);
        
        this.modeLabel = new JLabel("Drawing Mode:");
        this.weightLabel = new JLabel("Line Weight:");
        
        this.toolBox = new JPanel(toolLayout);
        
        this.fontTools = new JPanel(new FlowLayout());
        this.fontTools.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
        this.fontTools.setBorder(new MatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
        this.fontPicker = new JButton("Change Font");
        
        this.boldButton = new JToggleButton("Bold");
        this.italicButton = new JToggleButton("Italic");
        
        this.drawTools = new JPanel(new FlowLayout());
        this.drawTools.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
        this.drawTools.setBorder(new MatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
        
        this.colourPicker = new JButton("Change Colour");
        
        this.modeSelect = new JComboBox<>(DrawMode.values());
        this.modeSelect.setEditable(false);
        
        this.weightSelect = new JComboBox<>(new Integer[]{1, 2, 4, 8, 16});
        this.weightSelect.setEditable(false);
        
        this.setFilled = new JCheckBox("Filled:");
        this.setFilled.setHorizontalTextPosition(SwingConstants.LEFT);
        this.setFilled.setSelected(false);
        
        this.setBorder = new JCheckBox("Border:");
        this.setBorder.setHorizontalTextPosition(SwingConstants.LEFT);
        this.setBorder.setSelected(false);
        
        this.borderWLabel = new JLabel("Border Weight:");
        
        this.borderPicker = new JButton("Border Colour");
        this.borderPicker.setEnabled(false);
        
        this.borderWSelect = new JComboBox<>(new Integer[]{1, 2, 4, 8, 16});
        this.borderWSelect.setEditable(false);
        this.borderWSelect.setEnabled(false);
        
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
    }
    
    /**
     * Attach the various listeners to the components that need them.
     * 
     * @since 1.0
     */
    public void setupLayout()
    {
        colourPicker.addActionListener(this);
        modeSelect.addItemListener(this);
        
        fontPicker.addActionListener(this);
        boldButton.addActionListener(this);
        italicButton.addActionListener(this);
        
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
        
        fontTools.add(fontPicker);
        fontTools.add(boldButton);
        fontTools.add(italicButton);
        toolBox.add(fontTools, TEXT);
        
        drawTools.add(weightLabel);
        drawTools.add(weightSelect);
        drawTools.add(setFilled);
        drawTools.add(setBorder);
        drawTools.add(borderPicker);
        drawTools.add(borderWLabel);
        drawTools.add(borderWSelect);
        toolBox.add(drawTools, DRAWING);
        
        this.add(toolBox);
        
        disableFontControls();
        toolLayout.show(toolBox, DRAWING);
    }
    
    /**
     * Display the text editing controls.
     * 
     * @since 1.0
     */
    public void enableFontControls()
    {
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
    public void disableFontControls()
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
     * @since 1.0
     */
    public void enableDrawControls()
    {
        Component[] drawControls = drawTools.getComponents();
        for (Component com : drawControls) {
            com.setEnabled(true);
        }
        if (!borderShape) {
            borderPicker.setEnabled(false);
            borderWSelect.setEnabled(false);
        }
    }
    
    /**
     * Disable the shape editing and drawing tools.
     * 
     * @since 1.0
     */
    public void disableDrawControls()
    {
        Component[] drawControls = drawTools.getComponents();
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
    public void drawLine(Point nextPoint)
    {
        if (firstPoint == null) // Store the first point passed if none is held.
            firstPoint = nextPoint;
        if (lastPoint == null) {
            lastPoint = nextPoint;
            return;
        }
        
        lastPoint = canvas.drawLine(lastPoint, nextPoint, colour, lineWeight);
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
    public void drawRect(Point point)
    {
        if (firstPoint == null) {
            firstPoint = point;
        } else {
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
        }
    }
    
    /**
     * Draw a {@link String} to the referenced {@link WhiteboardCanvas}. The 
     * drawing point for this text is set by clicking the canvas before typing.
     * 
     * @param s The {@link String} to draw to the canvas.
     * @since 1.0
     */
    public void drawText(String s)
    {
        if (lastPoint != null)
            lastPoint = canvas.drawText(s, lastPoint, font, colour);
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
        if (e.getSource() == colourPicker)
            colour = showColourPicker();
        else if (e.getSource() == borderPicker)
            borderColour = showColourPicker();
        else if (e.getSource() == fontPicker)
            font = showFontChooser();
        else if (e.getSource() == boldButton)
            toggleBold();
        else if (e.getSource() == italicButton)
            toggleItalics();
        else
            System.err.printf("Unknown action! (%s)\n", e.paramString());
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
            case RECTANGLE:
                enableDrawControls();
                disableFontControls();
                toolLayout.show(toolBox, DRAWING);
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
            String str = Character.toString(e.getKeyChar());
            drawText(str);
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
                    default:
                        System.err.printf("Mouse button '%d' does nothing\n", 
                                e.getButton());
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
                    default:
                        System.err.printf("Mouse button '%d' does nothing\n", 
                                e.getButton());
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
