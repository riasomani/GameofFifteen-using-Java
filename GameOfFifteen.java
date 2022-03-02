import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;

// We are going to create a Game of Fifteen Puzzle.
public class GameOfFifteen extends JPanel {

	//Size of the puzzle
	private int size;
	//Number of Tiles
	private int nbTiles;
	//Grid UI Dimension
	private int dimension;
	//Foreground Colour
	private static final Color FOREGROUND_COLOUR = new Color(37, 200, 213); //use arbitrary colour
	//Random object to shuffle the tiles
	private static final Random RANDOM = new Random();
	//Array for storing the tiles
	private int[] tiles;
	//Size of tile on UI
	private int tileSize;
	//Position of the blank tile
	private int blankPos;
	//Margin for the grid on the frame
	private int margin;
	//Grid UI size
	private int gridSize;
	private boolean gameOver; //true if game over, else false
	
	public GameOfFifteen(int size, int dim, int mar)
	{
		this.size = size;
		dimension = dim;
		margin = mar;
		
		nbTiles = (size * size) - 1; //-1 since blank tiles is not to be counted
		tiles = new int[size * size];
		
		gridSize = (dim - 2 * margin);
		tileSize = gridSize / size;
		
		setPreferredSize(new Dimension(dimension, dimension + margin));
		setBackground(Color.WHITE);
		setForeground(FOREGROUND_COLOUR);
		setFont(new Font("SansSerif", Font.BOLD, 60));
		
		gameOver = true;
		
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				// used to let users to interact on the grid by clicking
				if(gameOver)
				{
					newGame();
				}
				else
				{
					// get position of the click
					int ex = e.getX() - margin;
					int ey = e.getY() - margin;
					
					if(ex < 0 || ex > gridSize || ey < 0 || ey > gridSize)
						return;
					
					// get position in the grid
					int c1 = ex / tileSize;
					int r1 = ey / tileSize;
					
					// get position of the blank cell
					int c2 = blankPos % size;
					int r2 = blankPos / size;
					
					// we convert it to 1D coordinates
					int clickPos = r1 * size + c1;
					
					int dir = 0;
					
					// we search direction for multiple tiles moves at once
					if(c1 == c2 && Math.abs(r1 - r2) > 0)
						dir = (r1 - r2) > 0 ? size : -size;
					else if(r1 == r2 && Math.abs(c1 - c2) > 0)
						dir = (c1 - c2) > 0 ? 1 : -1;
					
					if(dir != 0)
					{
						// we move tiles in the direction
						do
						{
							int newBlankPos = blankPos + dir;
							tiles[blankPos] = tiles[newBlankPos];
							blankPos = newBlankPos;
						} while(blankPos != clickPos);
						
						tiles[blankPos] = 0;
					}
					
					// check if game is solved
					gameOver = isSolved();
				}
				
				// we repaint the panel
				repaint();
			}
		});
		
		newGame();
	}
	
	private void newGame()
	{
		do {
			reset(); // reset in initial state
			shuffle();
		}while(!isSolvable()); // make it until solvable
	
		gameOver = false;
	}
	
	private void reset()
	{
		for(int i = 0; i < tiles.length; i++)
		{
			tiles[i] = (i + 1) % tiles.length;
		}
		
		// we set blank call at last
		blankPos = tiles.length - 1;
	}
	
	private void shuffle()
	{
		// we do not include blank tile here
		int n = nbTiles;
		
		while(n > 1)
		{
			int r = RANDOM.nextInt(n--);
			int tmp = tiles[r];
			tiles[r] = tiles[n];
			tiles[n] = tmp;
		}
	}
	
	// Only half permutations of the puzzle are solvable
	// Whenever a tile is preceded by a tile with higher value, it counts
	// as an inversion. With the blank tile in solved position,
	// the number of inversions must be even for the puzzle to be solvable
	private boolean isSolvable()
	{
		int countInversions = 0;
		
		for(int i = 0; i < nbTiles; i++)
		{
			for(int j = 0; j < i; j++)
			{
				if(tiles[j]>tiles[i])
					countInversions++;
			}
		}
		return countInversions % 2 == 0;
	}
	
	private boolean isSolved()
	{
		if(tiles[tiles.length - 1] != 0)
			return false;
		
		for(int i = nbTiles - 1; i>=0; i--)
		{
			if(tiles[i] != i+1)
				return false;
		}
		return true;
	}
	
	private void drawGrid(Graphics2D g)
	{
		for(int i = 0; i < tiles.length; i++)
		{	// converting 1D coordinates to 2D depending on given array size
			int r = i / size;
			int c = i % size;
			
			int x = margin + c * tileSize;
			int y = margin + r * tileSize;
			
			// check for special case for blank tile
			if(tiles[i] == 0)
			{
				if(gameOver)
				{
					g.setColor(FOREGROUND_COLOUR);
					drawCentredString(g, "\u2713", x, y);
				}
				
				continue;
			}
			
			// for other tiles
			g.setColor(getForeground());
			g.fillRoundRect(x, y, tileSize, tileSize, 25, 25);
			g.setColor(Color.BLACK);
			g.drawRoundRect(x, y, tileSize, tileSize, 25, 25);
			g.setColor(Color.WHITE);
			
			drawCentredString(g, String.valueOf(tiles[i]), x, y);
		}
	}
	
	private void drawStartMessage(Graphics2D g)
	{
		if(gameOver)
		{
			g.setFont(getFont().deriveFont(Font.BOLD, 20));
			g.setColor(FOREGROUND_COLOUR);
			String s = "Click to START A NEW GAME";
			g.drawString(s, (getWidth() - g.getFontMetrics().stringWidth(s)) / 2, getHeight() - margin);		
		}
	}
	
	private void drawCentredString(Graphics2D g, String s, int x, int y)
	{ 
		// center string s for the given tile (x,y)
		FontMetrics fm = g.getFontMetrics();
		int asc = fm.getAscent();
		int dsc = fm.getDescent();
		g.drawString(s, x + (tileSize - fm.stringWidth(s)) / 2, y + (asc + (tileSize - (asc +dsc)) / 2));
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawGrid(g2D);
		drawStartMessage(g2D);
	}
	
	public static void main(String[]args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setTitle("GAME OF FIFTEEN");
			frame.setResizable(false);
			frame.add(new GameOfFifteen(4, 550, 30), BorderLayout.CENTER);
			frame.pack();
			// center on the screen
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
}

