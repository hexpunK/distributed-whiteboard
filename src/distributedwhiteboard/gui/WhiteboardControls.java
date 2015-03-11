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
 *
 * @author Jordan
 */
public class WhiteboardControls 
        extends JPanel 
        implements ActionListener, 
                ItemListener, 
                KeyListener, 
                MouseListener,
                MouseMotionListener
{
    private static final long serialVersionUID = -6184851091001506327L;
    private static final String TEXT = "text";
    private static final String DRAWING = "drawing";
    
    private final WhiteboardCanvas canvas;
    private final FlowLayout layout;
    private final CardLayout toolLayout;
    private final JPanel toolBox, fontTools, drawTools;
    private final JComboBox<DrawMode> modeSelect;
    private final JComboBox<Integer> weightSelect, borderWSelect;
    private final JCheckBox setFilled, setBorder;
    private final JButton colourPicker, fontPicker, borderPicker;
    private final JToggleButton boldButton, italicButton;
    private final JLabel modeLabel, weightLabel, borderWLabel;
    private Point lastPoint, firstPoint;
    private DrawMode mode;
    private Color colour, borderColour;
    private Font font;
    private int lineWeight, borderWeight;
    private boolean fillShape, borderShape;
    
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
        this.borderWeight = -1;
        this.borderColour = Color.WHITE;
    }
    
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
    
    public void enableFontControls()
    {
        boldButton.setSelected(font.isBold());
        italicButton.setSelected(font.isItalic());
        Component[] fontControls = fontTools.getComponents();
        for (Component com : fontControls) {
            com.setEnabled(true);
        }
    }
    
    public void disableFontControls()
    {
        boldButton.setSelected(false);
        italicButton.setSelected(false);
        Component[] fontControls = fontTools.getComponents();
        for (Component com : fontControls) {
            com.setEnabled(false);
        }
    }
    
    public void enableDrawControls()
    {
        Component[] drawControls = drawTools.getComponents();
        for (Component com : drawControls) {
            com.setEnabled(true);
        }
    }
    
    public void disableDrawControls()
    {
        Component[] drawControls = drawTools.getComponents();
        for (Component com : drawControls) {
            com.setEnabled(false);
        }
    }
    
    public void drawLine(Point nextPoint)
    {
        if (firstPoint == null)
            firstPoint = nextPoint;
        if (lastPoint == null)
            lastPoint = nextPoint;
        else {
            if (lineWeight <= 0)
                lastPoint = canvas.drawLine(lastPoint, nextPoint, colour, 1);
            else
                lastPoint = canvas.drawLine(lastPoint, nextPoint, colour, lineWeight);
        }
    }
    
    public void drawRect(Point point)
    {
        if (firstPoint == null) {
            System.out.printf("Added rectangle origin - x: %d, y: %d\n", point.x, point.y);
            firstPoint = point;
        } else {
            Dimension rectSize = new Dimension(
                    point.x - firstPoint.x, 
                    point.y - firstPoint.y
            );
            System.out.printf("Set rectangle endpoint.\n\tPos - x: %d y: %d, w : %d, h: %d\n", point.x, point.y, rectSize.width, rectSize.height);
            if (rectSize.width < 0 && rectSize.height > 0) { // Point is top-right
                firstPoint = new Point(point.x, firstPoint.y);
            } else if (rectSize.width > 0 && rectSize.height < 0) { // Point is bottom-left
                firstPoint = new Point(firstPoint.x, point.y);
            } else if (rectSize.width < 0 && rectSize.height < 0) { // Point is bottom-right
                firstPoint = new Point(point.x, point.y);
            }
            lastPoint = canvas.drawRectangle(firstPoint, rectSize, colour, 
                    fillShape, borderShape, borderWeight, borderColour);
            firstPoint = null;
        }
    }
    
    public void drawText(String s)
    {
        if (lastPoint != null)
            lastPoint = canvas.drawText(s, lastPoint, font, colour);
    }
        
    private void showFontChooser()
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
            font = newFont;
        }
    }
    
    private Color showColourPicker()
    {
        Color newCol = JColorChooser.showDialog(this, "Choose Colour", colour);
        if (newCol != null)
            return newCol;
        
        return colour;
    }
    
    private void toggleBold()
    {
        if (boldButton.isSelected()) {
            if (italicButton.isSelected()) 
                font = font.deriveFont(Font.ITALIC|Font.BOLD);
            else
                font = font.deriveFont(Font.BOLD);
        } else {
            if (italicButton.isSelected())
                font = font.deriveFont(Font.ITALIC);
            else
                font = font.deriveFont(Font.PLAIN);
        }
    }
    
    private void toggleItalics()
    {
        if (italicButton.isSelected()) {
            if (boldButton.isSelected()) 
                font = font.deriveFont(Font.ITALIC|Font.BOLD);
            else
                font = font.deriveFont(Font.ITALIC);
        } else {
            if (boldButton.isSelected())
                font = font.deriveFont(Font.BOLD);
            else
                font = font.deriveFont(Font.PLAIN);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == colourPicker)
            colour = showColourPicker();
        else if (e.getSource() == borderPicker)
            borderColour = showColourPicker();
        else if (e.getSource() == fontPicker)
            showFontChooser();
        else if (e.getSource() == boldButton)
            toggleBold();
        else if (e.getSource() == italicButton)
            toggleItalics();
        else
            System.err.printf("Unknown action! (%s)\n", e.paramString());
    }
    
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
    
    private int sizeSelectAction(ItemEvent e)
    {
        Integer newSize = (Integer)e.getItem();
        return newSize;
    }
    
    private boolean checkBoxAction(ItemEvent e)
    {
        Boolean toggle = ((JCheckBox)e.getItem()).isSelected();
        return toggle;
    }

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

    @Override
    public void keyTyped(KeyEvent e)
    {
        if (mode.equals(DrawMode.TEXT)) {
            String str = Character.toString(e.getKeyChar());
            drawText(str);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        canvas.requestFocusInWindow();
        Point newPoint = e.getPoint();
        
        switch (mode) {
            case LINE:
            case POLYGON:
                switch(e.getButton()) {
                    case MouseEvent.BUTTON1:
                        System.out.printf("Added line point - x: %d, y: %d\n", newPoint.x, newPoint.y);
                        drawLine(newPoint);
                        break;
                    case MouseEvent.BUTTON3:
                        if (mode == DrawMode.POLYGON) {
                            System.out.println("Completing polygon.");
                            drawLine(firstPoint);
                        }
                        System.out.println("Cancelled line drawing.");
                        lastPoint = null;
                        firstPoint = null;
                        break;
                    default:
                        System.err.printf("Mouse button '%d' does nothing\n", e.getButton());
                }
                break;
            case RECTANGLE:
                switch(e.getButton()) {
                    case MouseEvent.BUTTON1:
                        drawRect(newPoint);
                        break;
                    case MouseEvent.BUTTON3:
                        System.out.println("Cancelled rectangle drawing.");
                        lastPoint = null;
                        firstPoint = null;
                        break;
                    default:
                        System.err.printf("Mouse button '%d' does nothing\n", e.getButton());
                }
                break;
            case TEXT:
                System.out.printf("Set text start point - x: %d, y: %d\n", newPoint.x, newPoint.y);
                lastPoint = newPoint;
                break;
            default:
                System.err.printf("Drawing mode %s not implemented.\n", mode);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        if (mode == DrawMode.FREEFORM_LINE)
            lastPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

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

    @Override
    public void mouseMoved(MouseEvent e) { }
}
