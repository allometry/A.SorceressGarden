import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
	public class Act {
		public Actor actor = null;
		public ArrayList<Elemental> primaryElemental = null;
		public ArrayList<Elemental> secondaryElemental = null;
		
		public Act() {
			primaryElemental = new ArrayList<Elemental>();
			secondaryElemental = new ArrayList<Elemental>();
		}
		
		public boolean isSafeForActorToMove() {
			boolean primaryElementalIsSafe = false, secondaryElementalIsSafe = false;
			
			if(primaryElemental.isEmpty() && secondaryElemental.isEmpty()) return true;
			
			for(Elemental primaryElement : primaryElemental)
				if(primaryElement.isElementalLookingAway() && primaryElement.isElementSafe())
					primaryElementalIsSafe = true;
			
			if(secondaryElemental.isEmpty()) {
				secondaryElementalIsSafe = true;
			} else {
				for(Elemental secondaryElement : secondaryElemental)
					if(secondaryElement.isElementalLookingAway() && secondaryElement.isElementSafe())
						secondaryElementalIsSafe = true;
			}
			
			if(secondaryElemental.isEmpty())
				return (primaryElementalIsSafe);
			else
				return (primaryElementalIsSafe && secondaryElementalIsSafe);
		}
	}
	
	public class Actor {
		public int cameraAngleMin;
		public int cameraAngleMax;
		public RSTile actorStartTile;
		public RSTile actorEndTile;
		public ArrayList<RSTile> actorSafeTiles = null;
		
		public Actor() {
			actorSafeTiles = new ArrayList<RSTile>();
		}
		
		public boolean isActorAtStart() {
			return (getMyPlayer().getLocation().equals(actorStartTile));
		}
		
		public boolean isActorSafe() {
			for(RSTile actorSafeTile : actorSafeTiles)
				if(getMyPlayer().getLocation().equals(actorSafeTile))
					return true;
			
			return false;
		}
		
		public boolean isActorAtEnd() {
			return (getMyPlayer().getLocation().equals(actorEndTile));
		}
	}
	
	public class Acts {
		public ArrayList<Act> acts = new ArrayList<Act>();
		
		public Act getCurrentAct() {
			for(int i = 0; i < acts.size(); i++) {
				if(acts.get(i).actor.isActorAtStart()) {
					return acts.get(i);
				}
			}
			
			return null;
		}
	}
	
	public class Elemental {
		public int elementalAngle;
		public int elementalNPCID;
		public RSArea elementalArea;
		
		public boolean isElementalLookingAway() {
			return (npcs.getNearest(elementalNPCID).getOrientation() == elementalAngle);
		}
		
		public boolean isElementSafe() {
			return (elementalArea.contains(npcs.getNearest(elementalNPCID).getLocation()));
		}
	}
	
	private int apprenticeNPC = 5532;
	private int broomstickItem = 14057;
	private int fountainObject = 21764;
	
	private Acts autumnGarden;
	
	private RSArea apprenticeArea = new RSArea(new RSTile(3318, 3137), new RSTile(3324, 3141));
	private RSArea bankingArea = new RSArea(new RSTile(3269, 3161), new RSTile(3272, 3173));
	private RSArea gardenLobbyArea = new RSArea(new RSTile(2903, 5463), new RSTile(2920,5480));
	private RSArea autumnSqirkTree = new RSArea(new RSTile(2909, 5448), new RSTile(2917, 5453));
	
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
	
	private enum State {
		startup("Starting..."),
		walkToAutumnGate("Walking to Autumn Gate"),
		openAutumnGate("Opening Autumn Gate"),
		continueAutumnSequence("Beginning Sequence"),
		runningAutumnSequence("Running Autumn Sequence");
		
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
		/*
		//Gate to Autumn Elemental 1
		autumnPlayerSequence0Start = new Actor(new RSTile(2913,5462));
		autumnPlayerSequence0End = new Actor(new RSTile(2908,5461));
		autumnSequence0 = new Sequence(autumnPlayerSequence0Start, autumnPlayerSequence0End, autumnPlayerSequence0SafeTiles);
		
		
		//Autumn Elemental 1
		autumnPlayerSequence1Start = new Actor(new RSTile(2908,5461));
		autumnPlayerSequence1End = new Actor(new RSTile(2902,5461));
		autumnElementalSequence1 = new Elemental(5533, 179, new RSArea(new RSTile(2903,5460),new RSTile(2905,5460)));
		autumnSequence1 = new Sequence(autumnPlayerSequence1Start, autumnPlayerSequence1End, autumnElementalSequence1);
		
		autumnPlayerSequence2Start = new Actor(new RSTile(2902,5461));
		autumnPlayerSequence2End = new Actor(new RSTile(2901,5458));
		autumnElementalSequence2 = new Elemental(5533, 0, new RSArea(new RSTile(2902,5460),new RSTile(2908,5460)));
		autumnSequence2 = new Sequence(autumnPlayerSequence2Start, autumnPlayerSequence2End, autumnElementalSequence2);
		
		//Autumn Elemental 1 to Autumn Elemental 2
		autumnPlayerSequence25Start = new Actor(new RSTile(2901,5458));
		autumnPlayerSequence25End = new Actor(new RSTile(2901,5455));
		autumnSequence25 = new Sequence(autumnPlayerSequence25Start, autumnPlayerSequence25End);
		
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
		autumnSequence.add(autumnSequence0);
		autumnSequence.add(autumnSequence1);
		autumnSequence.add(autumnSequence2);
		autumnSequence.add(autumnSequence25);
		autumnSequence.add(autumnSequence3);
		autumnSequence.add(autumnSequence4);
		autumnSequence.add(autumnSequence5);
		autumnSequence.add(autumnSequence6);
		*/
		
		autumnGarden = new Acts();
		
		//Gate to Safe Spot 0, 1st Autumn Elemental
		Act act0 = new Act();
		Actor actor0 = new Actor();
		
		actor0.actorStartTile = new RSTile(2913, 5462);
		actor0.actorEndTile = new RSTile(2908, 5461);
		
		act0.actor = actor0;
		autumnGarden.acts.add(act0);
		
		//Safe Spot 0 to Safe Spot 1, 1st Autumn Elemental
		Act act1 = new Act();
		Actor actor1 = new Actor();
		Elemental elemental1 = new Elemental();
		
		actor1.actorStartTile = new RSTile(2908, 5461);
		actor1.actorEndTile = new RSTile(2904, 5459);
		
		elemental1.elementalAngle = 179;
		elemental1.elementalNPCID = 5533;
		elemental1.elementalArea = new RSArea(new RSTile(2903, 5460), new RSTile(2905, 5460));
		
		act1.actor = actor1;
		act1.primaryElemental.add(elemental1);
		autumnGarden.acts.add(act1);
		
		//Safe Spot 1 to Safe Spot 2, 1st Autumn Elemental
		Act act2 = new Act();
		Actor actor2 = new Actor();
		Elemental elemental12 = new Elemental();
		
		actor2.actorStartTile = new RSTile(2904, 5459);
		actor2.actorEndTile = new RSTile(2901, 5458);
		
		elemental12.elementalAngle = 0;
		elemental12.elementalNPCID = 5533;
		elemental12.elementalArea = new RSArea(new RSTile(2904, 5460), new RSTile(2908, 5460));
		
		act2.actor = actor2;
		act2.primaryElemental.add(elemental12);
		autumnGarden.acts.add(act2);
		
		//Safe Spot 2 to Safe Spot 3, No Elemental
		Act act3 = new Act();
		Actor actor3 = new Actor();
		
		actor3.actorStartTile = new RSTile(2901, 5458);
		actor3.actorEndTile = new RSTile(2901, 5455);
		
		act3.actor = actor3;
		autumnGarden.acts.add(act3);
		
		//Safe Spot 3 to Safe Spot 4, 2nd Autumn Elemental
		Act act4 = new Act();
		Actor actor4 = new Actor();
		Elemental elemental24 = new Elemental();
		
		actor4.actorStartTile = new RSTile(2901, 5455);
		actor4.actorEndTile = new RSTile(2901, 5451);
		
		elemental24.elementalAngle = 270;
		elemental24.elementalNPCID = 5534;
		elemental24.elementalArea = new RSArea(new RSTile(2900, 5454), new RSTile(2900, 5452));
		
		act4.actor = actor4;
		act4.primaryElemental.add(elemental24);
		autumnGarden.acts.add(act4);
		
		//Safe Spot 4 to Safe Spot 5, 2nd & 3rd Autumn Elemental
		Act act5 = new Act();
		Actor actor5 = new Actor();
		Elemental elemental25 = new Elemental();
		Elemental elemental35 = new Elemental();
		
		actor5.actorStartTile = new RSTile(2901, 5451);
		actor5.actorEndTile = new RSTile(2903, 5450);
		
		elemental25.elementalAngle = 89;
		elemental25.elementalNPCID = 5534;
		elemental25.elementalArea = new RSArea(new RSTile(2900, 5450), new RSTile(2900, 5454));
		
		elemental35.elementalAngle = 0;
		elemental35.elementalNPCID = 5535;
		elemental35.elementalArea = new RSArea(new RSTile(2889, 5449), new RSTile(2902, 5449));
		
		act5.actor = actor5;
		act5.primaryElemental.add(elemental25);
		act5.secondaryElemental.add(elemental35);
		autumnGarden.acts.add(act5);
		
		//Safe Spot 5 to Safe Spot 6, 4th & 5th Autumn Elemental
		Act act6 = new Act();
		Actor actor6 = new Actor();
		Elemental elemental46 = new Elemental();
		Elemental elemental56 = new Elemental();
		
		actor6.actorStartTile = new RSTile(2901, 5451);
		actor6.actorEndTile = new RSTile(2903, 5450);
		
		elemental46.elementalAngle = 89;
		elemental46.elementalNPCID = 5534;
		elemental46.elementalArea = new RSArea(new RSTile(2900, 5450), new RSTile(2900, 5454));
		
		elemental56.elementalAngle = 0;
		elemental56.elementalNPCID = 5535;
		elemental56.elementalArea = new RSArea(new RSTile(2889, 5449), new RSTile(2903, 5449));
		
		act6.actor = actor6;
		act6.primaryElemental.add(elemental46);
		act6.secondaryElemental.add(elemental56);
		autumnGarden.acts.add(act5);
		
		/*
		 * //Autumn Elemental 2 & 3
		autumnPlayerSequence4Start = new Actor(new RSTile(2901,5451));
		autumnPlayerSequence4End = new Actor(new RSTile(2903,5450));
		autumnElementalSequence4Primary = new Elemental(5534, 89, new RSArea(new RSTile(2900,5450),new RSTile(2908,5454)));
		autumnElementalSequence4Dependent = new Elemental(5535, 0, new RSArea(new RSTile(2889,5449),new RSTile(2903,5449)));
		autumnSequence4 = new Sequence(autumnPlayerSequence4Start, autumnPlayerSequence4End, autumnElementalSequence4Primary, autumnElementalSequence4Dependent);
		 */
		
		return true;
	}
	
	Act nowPlaying;
	
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
		
		if(getMyPlayer().isMoving()) return 1;
		
		if(gardenLobbyArea.contains(getMyPlayer().getLocation())) {
			if(objects.getNearest(autumnGateObjectID).isOnScreen()) {
				objects.getNearest(autumnGateObjectID).doClick();
				return random(1700, 2500);
			} else {
				walking.walkTileMM(objects.getNearest(autumnGateObjectID).getLocation());
				return random(2600, 3000);
			}
		} else if(autumnSqirkTree.contains(getMyPlayer().getLocation())) {
			if(objects.getNearest(autumnSqirkTreeObject) != null) {
				objects.getNearest(autumnSqirkTreeObject).doClick();
				return random(2300, 2600);
			}
		} else {
			nowPlaying = autumnGarden.getCurrentAct();
			
			if(nowPlaying != null) {
				if(nowPlaying.actor.isActorAtStart()) {
					hoverMouseOverTile(nowPlaying.actor.actorEndTile);
					if(nowPlaying.isSafeForActorToMove()) {
						walking.walkTileOnScreen(nowPlaying.actor.actorEndTile);
						return random(1700, 2500);
					}
				}
			}
		}
		return 1;
	}
	
	public void hoverMouseOverTile(RSTile tile) {
		Point p = calc.tileToScreen(tile);
		p.x -= 15;
		p.y -= 15;
		Dimension size = new Dimension(30, 30);
		Rectangle tileRect = new Rectangle(p, size);
		
		if(!tileRect.contains(mouse.getLocation()))
			mouse.move(p, 15, 15);
	}
	
	public void onRepaint(final Graphics g2) {
		try {
			Graphics2D g = (Graphics2D)g2;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			g.setColor(Color.black);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
			g.drawString(": " + camera.getAngle(),15, 65);
			
			RSTile t = walking.getDestination();
			
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
	        g.drawRect(mouse.getLocation().x - 15, mouse.getLocation().y - 15, 30, 30);
		} catch(Exception e) {
			
		}
	}
	
	@Override
	public void onFinish() {
		return ;
	}
}
