import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import com.sun.org.apache.bcel.internal.generic.NEW;

@ScriptManifest(authors = { "Allometry" }, keywords = "Thieving Sourceress Garden", name = "A. Sorceress Garden", version = 0.1, description = "Start, all options are in GUI.")
public class ASorceressGarden extends Script implements PaintListener {
	private boolean isRunning = false;
	
	private class Sequence {
		private Elemental primaryElemental;
		private Elemental dependentElemental;
		
		private Actor playerStart;
		private Actor playerEnd;
		
		public Sequence(Actor playerStart, Actor playerEnd, Elemental primaryElemental) {
			this.playerStart = playerStart;
			this.playerEnd = playerEnd;
			this.primaryElemental = primaryElemental;
			this.dependentElemental = null;
		}
		
		public Sequence(Actor playerStart, Actor playerEnd, Elemental primaryElemental, Elemental dependentElemental) {
			this.playerStart = playerStart;
			this.playerEnd = playerEnd;
			this.primaryElemental = primaryElemental;
			this.dependentElemental = dependentElemental;
		}
		
		public Elemental getPrimaryElemental() {
			return primaryElemental;
		}

		public void setPrimaryElemental(Elemental primaryElemental) {
			this.primaryElemental = primaryElemental;
		}

		public Elemental getDependentElemental() {
			return dependentElemental;
		}

		public void setDependentElemental(Elemental dependentElemental) {
			this.dependentElemental = dependentElemental;
		}

		public Actor getPlayerStart() {
			return playerStart;
		}

		public void setPlayerStart(Actor playerStart) {
			this.playerStart = playerStart;
		}

		public Actor getPlayerEnd() {
			return playerEnd;
		}

		public void setPlayerEnd(Actor playerEnd) {
			this.playerEnd = playerEnd;
		}

		public boolean canPlayerGoToEnd() {
			if(getMyPlayer().getLocation().equals(playerStart.getPlayerTile())) {
				if(dependentElemental == null) {
					if(primaryElemental.getCurrentElementalOrientation() >= 0) {
						if(primaryElemental.getCurrentElementalOrientation() == primaryElemental.getElementalOrientation() && 
								primaryElemental.getElementalArea().contains(primaryElemental.getCurrentElementalLocation())) {
							return true;
						} else {
							return false;
						}
					} else {
						if(primaryElemental.getElementalArea().contains(primaryElemental.getCurrentElementalLocation())) {
							return true;
						} else {
							return false;
						}
					}
				} else {
					if(primaryElemental.getCurrentElementalOrientation() >= 0) {
						if(primaryElemental.getCurrentElementalOrientation() == primaryElemental.getElementalOrientation() && 
								primaryElemental.getElementalArea().contains(primaryElemental.getCurrentElementalLocation()) &&
								dependentElemental.getCurrentElementalOrientation() == dependentElemental.getElementalOrientation() &&
								dependentElemental.getElementalArea().contains(dependentElemental.getCurrentElementalLocation())) {
							return true;
						} else {
							return false;
						}
					} else {
						if(primaryElemental.getElementalArea().contains(primaryElemental.getCurrentElementalLocation()) &&
								dependentElemental.getCurrentElementalOrientation() == dependentElemental.getElementalOrientation() &&
								dependentElemental.getElementalArea().contains(dependentElemental.getCurrentElementalLocation())) {
							return true;
						} else {
							return false;
						}
					}
				}
			} else {
				//Player needs to be moved to starting area
				return false;
			}
		}
	}
	
	private class Elemental {
		private int elementalNPCID;
		private int elementalOrientation;
		private RSNPC elementalNPC;
		
		private RSArea elementalArea;
		private RSTile elementalTile;
		
		public Elemental(int elementalNPCID, int elementalOrientation, RSArea elementalArea) {
			this.elementalNPCID = elementalNPCID;
			this.elementalOrientation = elementalOrientation;
			this.elementalArea = elementalArea;
			this.elementalTile = null;
			this.elementalNPC = npcs.getNearest(elementalNPCID);
		}
		
		public Elemental(int elementalNPCID, int elementalOrientation, RSTile elementalTile) {
			this.elementalNPCID = elementalNPCID;
			this.elementalOrientation = elementalOrientation;
			this.elementalArea = null;
			this.elementalTile = elementalTile;
			this.elementalNPC = npcs.getNearest(elementalNPCID);
		}

		public int getElementalNPCID() {
			return elementalNPCID;
		}

		public void setElementalNPCID(int elementalNPCID) {
			this.elementalNPCID = elementalNPCID;
		}
		
		public RSTile getCurrentElementalLocation() {
			this.elementalNPC = npcs.getNearest(elementalNPCID);
			return this.elementalNPC.getLocation();
		}
		
		public int getCurrentElementalOrientation() {
			this.elementalNPC = npcs.getNearest(elementalNPCID);
			return this.elementalNPC.getOrientation();
		}

		public int getElementalOrientation() {
			return elementalOrientation;
		}

		public void setElementalOrientation(int elementalOrientation) {
			this.elementalOrientation = elementalOrientation;
		}

		public RSArea getElementalArea() {
			return elementalArea;
		}

		public void setElementalArea(RSArea elementalArea) {
			this.elementalArea = elementalArea;
		}

		public RSTile getElementalTile() {
			return elementalTile;
		}

		public void setElementalTile(RSTile elementalTile) {
			this.elementalTile = elementalTile;
		}
	}
	
	private class Actor {
		private RSArea playerArea;
		private RSTile playerTile;
		
		public Actor(RSArea playerArea) {
			this.playerArea = playerArea;
			this.playerTile = null;
		}
		
		public Actor(RSTile playerTile) {
			this.playerArea = null;
			this.playerTile = playerTile;
		}

		public RSArea getPlayerArea() {
			return playerArea;
		}

		public void setPlayerArea(RSArea playerArea) {
			this.playerArea = playerArea;
		}

		public RSTile getPlayerTile() {
			return playerTile;
		}

		public void setPlayerTile(RSTile playerTile) {
			this.playerTile = playerTile;
		}
	}

	private int apprenticeNPC = 5532;
	private int broomstickItem = 14057;
	private int fountainObject = 21764;
	
	private RSArea apprenticeArea = new RSArea(new RSTile(3318, 3137), new RSTile(3324, 3141));
	private RSArea bankingArea = new RSArea(new RSTile(3269, 3161), new RSTile(3272, 3173));
	private RSArea gardenLobbyArea = new RSArea(new RSTile(2903, 5463), new RSTile(2920,5480));
	
	private Sequence currentSequence;
	
	private String apprenticeAction = "Teleport";
	private String broomstickAction = "Sorceress's Garden";
	private String fountainAction = "Drink-from";
	private String sqirkAction = "Pick-fruit";
	private String success = "An elemental force emanating from the garden teleports you away.";
	
	/*
	 * Autumn Sequence
	 */
	private int autumnGateObjectID = 21731;
	private int autumnSqirkTreeObject = 21768;
	private int autumnSqirkItem = 10846;
	
	private Actor autumnPlayerSequence1Start;
	private Actor autumnPlayerSequence1End;
	private Elemental autumnElementalSequence1;
	private Sequence autumnSequence1;
	
	private Actor autumnPlayerSequence2Start;
	private Actor autumnPlayerSequence2End;
	private Elemental autumnElementalSequence2;
	private Sequence autumnSequence2;
	
	private Actor autumnPlayerSequence3Start;
	private Actor autumnPlayerSequence3End;
	private Elemental autumnElementalSequence3;
	private Sequence autumnSequence3;
	
	private Actor autumnPlayerSequence4Start;
	private Actor autumnPlayerSequence4End;
	private Elemental autumnElementalSequence4Primary;
	private Elemental autumnElementalSequence4Dependent;
	private Sequence autumnSequence4;
	
	private Actor autumnPlayerSequence5Start;
	private Actor autumnPlayerSequence5End;
	private Elemental autumnElementalSequence5Primary;
	private Elemental autumnElementalSequence5Dependent;
	private Sequence autumnSequence5;
	
	private Actor autumnPlayerSequence6Start;
	private Actor autumnPlayerSequence6End;
	private Elemental autumnElementalSequence6;
	private Sequence autumnSequence6;
	
	private ArrayList<Sequence> autumnSequence;
	
	private enum State {
		startup("Starting..."),
		openAutumnGate("Opening Autumn Gate"),
		beginAutumnSequence("Beginning Sequence"),
		runAutumnSequence("Running Autumn Sequence");
		
		private String message;
		State(String message) {
			this.message = message;
		}
		
		public String getMessage() {
			return message;
		}
	}
	private State state = State.startup;
	
	@Override
	public boolean onStart() {
		log("Started...");
		
		/*
		 * Autumn Sequence
		 */
		//Autumn Elemental 1
		autumnPlayerSequence1Start = new Actor(new RSTile(2908,5461));
		autumnPlayerSequence1End = new Actor(new RSTile(2902,5461));
		autumnElementalSequence1 = new Elemental(5533, 179, new RSArea(new RSTile(2903,5460),new RSTile(2905,5460)));
		autumnSequence1 = new Sequence(autumnPlayerSequence1Start, autumnPlayerSequence1End, autumnElementalSequence1);
		
		autumnPlayerSequence2Start = new Actor(new RSTile(2902,5461));
		autumnPlayerSequence2End = new Actor(new RSTile(2901,5455));
		autumnElementalSequence2 = new Elemental(5533, 0, new RSArea(new RSTile(2902,5460),new RSTile(2908,5460)));
		autumnSequence2 = new Sequence(autumnPlayerSequence2Start, autumnPlayerSequence2End, autumnElementalSequence2);
		
		//Autumn Elemental 2
		autumnPlayerSequence3Start = new Actor(new RSTile(2901,5455));
		autumnPlayerSequence3End = new Actor(new RSTile(2901,5451));
		autumnElementalSequence3 = new Elemental(5534, 270, new RSArea(new RSTile(2900,5454),new RSTile(2900,5452)));
		autumnSequence3 = new Sequence(autumnPlayerSequence3Start, autumnPlayerSequence3End, autumnElementalSequence3);
		
		//Autumn Elemental 2 & 3
		autumnPlayerSequence4Start = new Actor(new RSTile(2901,5451));
		autumnPlayerSequence4End = new Actor(new RSTile(2903,5450));
		autumnElementalSequence4Primary = new Elemental(5534, 89, new RSArea(new RSTile(2900,5450),new RSTile(2908,5454)));
		autumnElementalSequence4Dependent = new Elemental(5535, 0, new RSArea(new RSTile(2889,5449),new RSTile(2903,5449)));
		autumnSequence4 = new Sequence(autumnPlayerSequence4Start, autumnPlayerSequence4End, autumnElementalSequence4Primary, autumnElementalSequence4Dependent);
		
		//Autumn Elemental 4 & 5
		autumnPlayerSequence5Start = new Actor(new RSTile(2903,5450));
		autumnPlayerSequence5End = new Actor(new RSTile(2908,5456));
		autumnElementalSequence5Primary = new Elemental(5536, -1, new RSArea(new RSTile(2903,5453),new RSTile(2905,5455)));
		autumnElementalSequence5Dependent = new Elemental(5537, 0, new RSArea(new RSTile(2904,5457),new RSTile(2917,5457)));
		autumnSequence5 = new Sequence(autumnPlayerSequence5Start, autumnPlayerSequence5End, autumnElementalSequence5Primary, autumnElementalSequence5Dependent);
		
		//Autumn Elemental 6
		autumnPlayerSequence6Start = new Actor(new RSTile(2908,5456));
		autumnPlayerSequence6End = new Actor(new RSArea(new RSTile(2909,5448), new RSTile(2917,5453)));
		autumnElementalSequence6 = new Elemental(5538, 0, new RSArea(new RSTile(2911,5455),new RSTile(2916,5455)));
		autumnSequence6 = new Sequence(autumnPlayerSequence6Start, autumnPlayerSequence6End, autumnElementalSequence6);
		
		autumnSequence = new ArrayList<Sequence>();
		autumnSequence.add(autumnSequence1);
		autumnSequence.add(autumnSequence2);
		autumnSequence.add(autumnSequence3);
		autumnSequence.add(autumnSequence4);
		autumnSequence.add(autumnSequence5);
		autumnSequence.add(autumnSequence6);
		
		return true;
	}
	
	@Override
	public int loop() {
		/*
		 * Storyboard
		 * 
		 * If player's inventory is full of sq'irk fruit, teleport to daemonheim. Restart loop.
		 * If player's inventory is full of sq'irk fruit and is in daemonheim, bank.
		 * If player's inventory is empty and is in daemonheim, teleport to garden with broomstick.
		 * 
		 * If player is in the garden lobby, open the gate to enter
		 * Ask to find the proper sequence
		 * If null is returned, default to selecting the first sequence in the sequences array and move the player to the starting position. Restart loop.
		 * Else select the sequence and execute the proper move when Sequence::canPlayerMove returns true
		 * 
		 * If the player is in the Sq'irk Tree area, pick the fruit and Restart loop.
		 */
		updateState();
		log("trying");
		try {
			switch(state) {
				case openAutumnGate:
					RSObject autumnGateObject = objects.getNearest(autumnGateObjectID);
					
					if(autumnGateObject != null) {
						if(autumnGateObject.isOnScreen()) {
							do {
								if(!getMyPlayer().isMoving()) {
									autumnGateObject.doClick();
									
									sleep(random(1700, 2000));
									
									updateState();
								}
							} while(state.equals(State.openAutumnGate));
						} else {
							camera.turnToObject(autumnGateObject);
						}
					}
				break;
				
				case beginAutumnSequence:					
					if(!getMyPlayer().isMoving()) {
						currentSequence = retrieveProperSequence(autumnSequence);
						
						log(currentSequence.getPlayerStart().getPlayerTile().toString());
						
						walking.walkTileOnScreen(currentSequence.getPlayerStart().getPlayerTile());
					}
				break;
				
				case runAutumnSequence:
					if(!getMyPlayer().isMoving()) {
						currentSequence = retrieveProperSequence(autumnSequence);
						
						if(currentSequence.canPlayerGoToEnd())
							walking.walkTileOnScreen(currentSequence.getPlayerEnd().getPlayerTile());
					}
				break;
			}
		} catch(Exception e) {
			
		}
		
		return 1;
	}
	
	public void onRepaint(final Graphics g2) {
		try {
			Graphics2D g = (Graphics2D)g2;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			g.setColor(Color.black);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			g.drawString(": " + state.getMessage(),15, 65);
			
			RSTile t = currentSequence.getPlayerEnd().getPlayerTile();
			
			//Temporary tile highlight code used for debugging.
			//Kindly borrowed from GodlessFisher, by Enfilade.
			Point[] points = new Point[12];

	        points[0] = calc.tileToScreen(t, 0, 0.75, 0);
	        points[1] = calc.tileToScreen(t, 0, 1, 0);
	        points[2] = calc.tileToScreen(t, 0.25, 0.99, 0);

	        points[3] = calc.tileToScreen(t, 0.75, 0.99, 0);
	        points[4] = calc.tileToScreen(t, 0.99, 0.99, 0);
	        points[5] = calc.tileToScreen(t, 0.99, 0.75, 0);

	        points[6] = calc.tileToScreen(t, 0.99, 0.25, 0);
	        points[7] = calc.tileToScreen(t, 0.99, 0, 0);
	        points[8] = calc.tileToScreen(t, 0.75, 0, 0);

	        points[9] = calc.tileToScreen(t, 0.25, 0, 0);
	        points[10] = calc.tileToScreen(t, 0, 0, 0);
	        points[11] = calc.tileToScreen(t, 0, 0.25, 0);

	        g.setColor(Color.BLACK);
	        for(int i = 0; i < 12; i += 3) {
	            g.drawLine(points[i].x, points[i].y, points[i+1].x, points[i+1].y);
	            g.drawLine(points[i+1].x, points[i+1].y, points[i+2].x, points[i+2].y);
	        }

	        points[0] = calc.tileToScreen(t, 0.40, 0.5, 0);
	        points[1] = calc.tileToScreen(t, 0.60, 0.5, 0);
	        points[2] = calc.tileToScreen(t, 0.5, 0.40, 0);
	        points[3] = calc.tileToScreen(t, 0.5, 0.60, 0);

	        g.setColor(Color.RED);
	        g.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
	        g.drawLine(points[2].x, points[2].y, points[3].x, points[3].y);
		} catch(Exception e) {
			
		}
	}
	
	@Override
	public void onFinish() {
		return ;
	}
	
	public Sequence retrieveProperSequence(ArrayList<Sequence> sequences) {
		for (Sequence sequence : sequences)
			if(sequence.getPlayerStart().getPlayerArea() == null)
				if(sequence.getPlayerStart().getPlayerTile().equals(getMyPlayer().getLocation()))
					return sequence;
			else
				if(sequence.getPlayerStart().getPlayerArea().contains(getMyPlayer().getLocation()))
					return sequence;
		
		return sequences.get(0);
	}
	
	public void updateState() {
		if(inventory.isFull()) {
			
		} else {
			if(gardenLobbyArea.contains(getMyPlayer().getLocation())) {
				state = State.openAutumnGate;
			} else {
				if(retrieveProperSequence(autumnSequence).equals(autumnSequence.get(0))) {
					state = State.beginAutumnSequence;
				} else {
					state = State.runAutumnSequence;
				}
			}
		}
	}
}
