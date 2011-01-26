import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = { "Allometry" }, keywords = "Thieving Sourceress Garden", name = "A. Sorceress Garden", version = 0.1, description = "Start, all options are in GUI.")
public class ASorceressGarden extends Script implements MessageListener, PaintListener {
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
			
			for(Elemental primaryElement : primaryElemental) {
				if(primaryElement.isElementalLookingAway() && primaryElement.isElementSafe()) {
					primaryElementalIsSafe = true;
					break;
				}
			}
			
			if(secondaryElemental.isEmpty()) {
				secondaryElementalIsSafe = true;
			} else {
				for(Elemental secondaryElement : secondaryElemental) {
					if(secondaryElement.isElementalLookingAway() && secondaryElement.isElementSafe()) {
						secondaryElementalIsSafe = true;
						break;
					}
				}
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
	
	private boolean isScriptLoaded = false, havePickedGardenFruit = false;
	
	private int apprenticeNPCID = 5532;
	private int broomstickItemID = 14057;
	private int fountainObjectID = 21764;
	private int fremennikBankerNPCID = 9710;
	
	private int accumulatedFruit = 0, accumulatedFail = 0;
	private int pickedFruitWidgetIndex, failedMazeWidgetIndex;
	private int currentRuntimeWidgetIndex, farmingXPEarnedWidgetIndex, theivingXPEarnedWidgetIndex;
	private int startingFarmingXP;
	
	private long startingTime;
	
	private Act nowPlaying;
	private Acts autumnGarden, summerGarden, garden;
	
	private Image brushImage, clockImage, cursorImage, doNotEnterImage, maskImage, treeImage;
	private ImageObserver observer;
	
	private NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.US);
	
	private RSArea apprenticeArea = new RSArea(new RSTile(3318, 3137), new RSTile(3324, 3141));
	private RSArea bankingArea = new RSArea(new RSTile(3439, 3693), new RSTile(3459, 3737));
	private RSTile[] bankingPath = {
			new RSTile(3449, 3704),
			new RSTile(3449, 3719)
	};
	private RSArea gardenLobbyArea = new RSArea(new RSTile(2903, 5463), new RSTile(2920,5480));
	
	private RSArea autumnSqirkTree = new RSArea(new RSTile(2909, 5448), new RSTile(2917, 5453));
	private RSArea summerSqirkTree = new RSArea(new RSTile(2912, 5487), new RSTile(2919, 5495));
	private RSArea gardenSqirkTree;
	
	private Scoreboard topLeftScoreboard, topRightScoreboard;

	private ScoreboardWidget pickedFruit, failedMaze;
	private ScoreboardWidget currentRuntime, farmingXPEarned, theivingXPEarned, approxTheivingXPEarned;
	
	private String apprenticeAction = "Teleport";
	private String broomstickAction = "Sorceress's Garden";
	private String fountainAction = "Drink-from";
	private String sqirkAction = "Pick-fruit";
	private String success = "An elemental force emanating from the garden teleports you away.";
	
	private int autumnGateObjectID = 21731;
	private int autumnSqirkTreeObjectID = 21768;
	private int autumnSqirkItemID = 10846;
	
	private int summerGateObjectID = 21687;
	private int summerSqirkTreeObjectID = 21766;
	private int summerSqirkItemID = 10845;
	
	private int gardenGateObjectID, gardenSqirkTreeObjectID, gardenSqirkItemID;
	
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
		try {
			brushImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/user_green.png"));
			cursorImage = ImageIO.read(new URL("http://scripts.allometry.com/app/webroot/img/cursors/cursor-01.png"));
			clockImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/time.png"));
			doNotEnterImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/delete.png"));
			maskImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/user_gray.png"));
			treeImage = ImageIO.read(new URL("http://scripts.allometry.com/icons/accept.png"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pickedFruit = new ScoreboardWidget(treeImage, "");
		failedMaze = new ScoreboardWidget(doNotEnterImage, "");
		
		currentRuntime = new ScoreboardWidget(clockImage, "");
		farmingXPEarned = new ScoreboardWidget(brushImage, "");
		theivingXPEarned = new ScoreboardWidget(maskImage, "");
		
		topLeftScoreboard = new Scoreboard(Scoreboard.TOP_LEFT, 128, 5);
		topLeftScoreboard.addWidget(pickedFruit);
		pickedFruitWidgetIndex = 0;
		topLeftScoreboard.addWidget(failedMaze);
		failedMazeWidgetIndex = 1;
		
		topRightScoreboard = new Scoreboard(Scoreboard.TOP_RIGHT, 128, 5);
		topRightScoreboard.addWidget(currentRuntime);
		currentRuntimeWidgetIndex = 0;
		topRightScoreboard.addWidget(farmingXPEarned);
		farmingXPEarnedWidgetIndex = 1;
		topRightScoreboard.addWidget(theivingXPEarned);
		theivingXPEarnedWidgetIndex = 2;
		
		autumnGarden = new Acts();
		summerGarden = new Acts();
		
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
		actor2.actorEndTile = new RSTile(2901, 5455);
		
		elemental12.elementalAngle = 0;
		elemental12.elementalNPCID = 5533;
		elemental12.elementalArea = new RSArea(new RSTile(2904, 5460), new RSTile(2908, 5460));
		
		act2.actor = actor2;
		act2.primaryElemental.add(elemental12);
		autumnGarden.acts.add(act2);
		
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
		elemental35.elementalArea = new RSArea(new RSTile(2889, 5449), new RSTile(2901, 5449));
		
		act5.actor = actor5;
		act5.primaryElemental.add(elemental25);
		act5.secondaryElemental.add(elemental35);
		autumnGarden.acts.add(act5);
		
		//Safe Spot 5 to Safe Spot 6, 4th & 5th Autumn Elemental
		Act act6 = new Act();
		Actor actor6 = new Actor();
		Elemental elemental461 = new Elemental();
		Elemental elemental462 = new Elemental();
		Elemental elemental463 = new Elemental();
		Elemental elemental56 = new Elemental();
		
		actor6.actorStartTile = new RSTile(2903, 5450);
		actor6.actorEndTile = new RSTile(2908, 5456);
		
		elemental461.elementalAngle = 89;
		elemental461.elementalNPCID = 5536;
		elemental461.elementalArea = new RSArea(new RSTile(2902, 5453), new RSTile(2903, 5455));
		
		elemental462.elementalAngle = 0;
		elemental462.elementalNPCID = 5536;
		elemental462.elementalArea = new RSArea(new RSTile(2903, 5455), new RSTile(2903, 5455));
		
		elemental463.elementalAngle = 270;
		elemental463.elementalNPCID = 5536;
		elemental463.elementalArea = new RSArea(new RSTile(2905, 5453), new RSTile(2905, 5455));
		
		elemental56.elementalAngle = 0;
		elemental56.elementalNPCID = 5537;
		elemental56.elementalArea = new RSArea(new RSTile(2904, 5457), new RSTile(2908, 5457));
		
		act6.actor = actor6;
		act6.primaryElemental.add(elemental461);
		act6.primaryElemental.add(elemental462);
		act6.primaryElemental.add(elemental463);
		act6.secondaryElemental.add(elemental56);
		autumnGarden.acts.add(act6);
		
		//Safe Spot 1 to Safe Spot 2, 6th Autumn Elemental
		Act act7 = new Act();
		Actor actor7 = new Actor();
		Elemental elemental67 = new Elemental();
		
		actor7.actorStartTile = new RSTile(2908, 5456);
		actor7.actorEndTile = autumnSqirkTree.getNearestTile(new RSTile(2908, 5456));
		
		elemental67.elementalAngle = 0;
		elemental67.elementalNPCID = 5538;
		elemental67.elementalArea = new RSArea(new RSTile(2911, 5455), new RSTile(2914, 5455));
		
		act7.actor = actor7;
		act7.primaryElemental.add(elemental67);
		autumnGarden.acts.add(act7);
		
		/*
		 * Summer Garden
		 * 
		 * Act 0
		 * Start 2910, 5481
		 * End 2908, 5482
		 * 
		 */
		Act summerAct0 = new Act();
		Actor summerActor0 = new Actor();
		summerActor0.actorStartTile = new RSTile(2910, 5481);
		summerActor0.actorEndTile = new RSTile(2908, 5482);
		summerActor0.cameraAngleMin = 90;
		summerActor0.cameraAngleMax = 270;
		summerAct0.actor = summerActor0;
		summerGarden.acts.add(summerAct0);
		
		/* 
		 * Act 1
		 * Start 2908, 5482
		 * End 2906, 5486
		 * Elemental 5547
		 * Orientation 89
		 * Area 2907, 5484 to 2907, 5486
		 * 
		 */
		Act summerAct1 = new Act();
		Actor summerActor1 = new Actor();
		Elemental summerElemental11 = new Elemental();
		summerActor1.actorStartTile = new RSTile(2908, 5482);
		summerActor1.actorEndTile = new RSTile(2906, 5486);
		summerActor1.cameraAngleMin = 30;
		summerActor1.cameraAngleMax = 125;
		summerElemental11.elementalAngle = 89;
		summerElemental11.elementalNPCID = 5547;
		summerElemental11.elementalArea = new RSArea(new RSTile(2907, 5484), new RSTile(2907, 5486));
		summerAct1.actor = summerActor1;
		summerAct1.primaryElemental.add(summerElemental11);
		summerGarden.acts.add(summerAct1);
		
		/*
		 * Act 2
		 * Start 2906, 5486
		 * End 2906, 5492
		 * Elemental 5547
		 * Orientation 270
		 * Area 2907, 5483 to 2907, 5486
		 * 
		 */
		Act summerAct2 = new Act();
		Actor summerActor2 = new Actor();
		Elemental summerElemental12 = new Elemental();
		summerActor2.actorStartTile = new RSTile(2906, 5486);
		summerActor2.actorEndTile = new RSTile(2906, 5492);
		summerActor2.cameraAngleMin = 35;
		summerActor2.cameraAngleMax = 125;
		summerElemental12.elementalAngle = 270;
		summerElemental12.elementalNPCID = 5547;
		summerElemental12.elementalArea = new RSArea(new RSTile(2907, 5483), new RSTile(2907, 5486));
		summerAct2.actor = summerActor2;
		summerAct2.primaryElemental.add(summerElemental12);
		summerGarden.acts.add(summerAct2);
		
		/*
		 * Act 3
		 * Start 2906, 5492
		 * End 2909, 5490
		 * Elemental 5548
		 * Orientation 270
		 * Area 2907, 5490 to 2907, 5492
		 * 
		 */
		Act summerAct3 = new Act();
		Actor summerActor3 = new Actor();
		Elemental summerElemental23 = new Elemental();
		summerActor3.actorStartTile = new RSTile(2906, 5492);
		summerActor3.actorEndTile = new RSTile(2909, 5490);
		summerElemental23.elementalAngle = 270;
		summerElemental23.elementalNPCID = 5548;
		summerElemental23.elementalArea = new RSArea(new RSTile(2907, 5490), new RSTile(2907, 5492));
		summerAct3.actor = summerActor3;
		summerAct3.primaryElemental.add(summerElemental23);
		summerGarden.acts.add(summerAct3);
		
		/*
		 * Act 4
		 * Start 2909, 5490
		 * End 2911, 5485
		 * Elemental 5549
		 * Orientation 89
		 * Area 2910, 5490 to 2910, 5493
		 * 
		 */
		Act summerAct4 = new Act();
		Actor summerActor4 = new Actor();
		Elemental summerElemental34 = new Elemental();
		summerActor4.actorStartTile = new RSTile(2909, 5490);
		summerActor4.actorEndTile = new RSTile(2911, 5485);
		summerActor4.cameraAngleMin = 90;
		summerActor4.cameraAngleMax = 145;
		summerElemental34.elementalAngle = 89;
		summerElemental34.elementalNPCID = 5549;
		summerElemental34.elementalArea = new RSArea(new RSTile(2910, 5489), new RSTile(2910, 5492));
		summerAct4.actor = summerActor4;
		summerAct4.primaryElemental.add(summerElemental34);
		summerGarden.acts.add(summerAct4);
		
		/*
		 * Act 5
		 * Start 2911, 5485
		 * End 2919, 5485
		 * 
		 * Elemental 5550
		 * Orientation 270
		 * Area 2915, 5483 to 2915, 5485
		 * 
		 * Elemental 5550
		 * Orientation 179
		 * Area 2912, 5483 to 2915, 5483
		 * 
		 */
		Act summerAct5 = new Act();
		Actor summerActor5 = new Actor();
		Elemental summerElemental451 = new Elemental();
		Elemental summerElemental452 = new Elemental();
		summerActor5.actorStartTile = new RSTile(2911, 5485);
		summerActor5.actorEndTile = new RSTile(2919, 5485);
		summerActor5.cameraAngleMin = 208;
		summerActor5.cameraAngleMax = 215;
		summerElemental451.elementalAngle = 270;
		summerElemental451.elementalNPCID = 5550;
		summerElemental451.elementalArea = new RSArea(new RSTile(2915, 5483), new RSTile(2915, 5485));
		summerElemental452.elementalAngle = 179;
		summerElemental452.elementalNPCID = 5550;
		summerElemental452.elementalArea = new RSArea(new RSTile(2912, 5483), new RSTile(2915, 5483));
		summerAct5.actor = summerActor5;
		summerAct5.primaryElemental.add(summerElemental451);
		summerAct5.primaryElemental.add(summerElemental452);
		summerGarden.acts.add(summerAct5);
		
		 /*
		 * Act 6
		 * Start 2919, 5485
		 * End 2924, 5487
		 * Elemental 5551
		 * Orientation 89
		 * Area 2923, 5486 to 2923, 5490
		 * 
		 */
		Act summerAct6 = new Act();
		Actor summerActor6 = new Actor();
		Elemental summerElemental56 = new Elemental();
		summerActor6.actorStartTile = new RSTile(2919, 5485);
		summerActor6.actorEndTile = new RSTile(2924, 5487);
		summerElemental56.elementalAngle = 89;
		summerElemental56.elementalNPCID = 5551;
		summerElemental56.elementalArea = new RSArea(new RSTile(2923, 5486), new RSTile(2923, 5490));
		summerAct6.actor = summerActor6;
		summerAct6.primaryElemental.add(summerElemental56);
		summerGarden.acts.add(summerAct6);
		/*
		 * Act 7
		 * Start 2924, 5487
		 * End 2919, 5488
		 * 
		 * Elemental 5551
		 * Orientation 270 (going south)
		 * Area 2923, 5487 to 2923, 5486
		 * 
		 * Elemental 5551
		 * Orientation 179 (going west)
		 * Area 2921, 5486 to 2923, 5486
		 * 
		 * Elemental 5552
		 * Orientation 0 (going east)
		 * Area 2921, 5491 to 2923, 5491
		 * 
		 * Elemental 5552
		 * Orientation 89 (going north)
		 * Area 2923, 5491 to 2923, 5495
		 * 
		 */
		Act summerAct7 = new Act();
		Actor summerActor7 = new Actor();
		Elemental summerElemental571 = new Elemental();
		Elemental summerElemental572 = new Elemental();
		Elemental summerElemental671 = new Elemental();
		Elemental summerElemental672 = new Elemental();
		summerActor7.actorStartTile = new RSTile(2924, 5487);
		summerActor7.actorEndTile = new RSTile(2919, 5488);
		summerElemental571.elementalAngle = 270;
		summerElemental571.elementalNPCID = 5551;
		summerElemental571.elementalArea = new RSArea(new RSTile(2923, 5487), new RSTile(2923, 5486));
		summerElemental572.elementalAngle = 179;
		summerElemental572.elementalNPCID = 5551;
		summerElemental572.elementalArea = new RSArea(new RSTile(2921, 5486), new RSTile(2923, 5486));
		summerElemental671.elementalAngle = 0;
		summerElemental671.elementalNPCID = 5552;
		summerElemental671.elementalArea = new RSArea(new RSTile(2921, 5491), new RSTile(2923, 5491));
		summerElemental672.elementalAngle = 89;
		summerElemental672.elementalNPCID = 5552;
		summerElemental672.elementalArea = new RSArea(new RSTile(2923, 5491), new RSTile(2923, 5495));
		summerAct7.actor = summerActor7;
		summerAct7.primaryElemental.add(summerElemental571);
		summerAct7.primaryElemental.add(summerElemental572);
		summerAct7.secondaryElemental.add(summerElemental671);
		summerAct7.secondaryElemental.add(summerElemental672);
		summerGarden.acts.add(summerAct7);
		
		int theivingLevel = skills.getCurrentLevel(Skills.THIEVING);
		if(theivingLevel >= 45 && theivingLevel < 65) {
			gardenGateObjectID = autumnGateObjectID;
			gardenSqirkTree = autumnSqirkTree;
			gardenSqirkItemID = autumnSqirkItemID;
			gardenSqirkTreeObjectID = autumnSqirkTreeObjectID;
			garden = autumnGarden;
		} else {
			gardenGateObjectID = summerGateObjectID;
			gardenSqirkTree = summerSqirkTree;
			gardenSqirkItemID = summerSqirkItemID;
			gardenSqirkTreeObjectID = summerSqirkTreeObjectID;
			garden = summerGarden;
		}
		
		isScriptLoaded = true;
		startingTime = System.currentTimeMillis();
		startingFarmingXP = skills.getCurrentExp(Skills.FARMING);
		
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
		
		if(getMyPlayer().isMoving()) return 1;
		
		if(inventory.isFull()) {
			if(gardenLobbyArea.contains(getMyPlayer().getLocation())) {
				if(game.getCurrentTab() != Game.TAB_EQUIPMENT) {
					game.openTab(Game.TAB_EQUIPMENT);
				}
				
				equipment.getItem(Equipment.RING).doAction("Teleport");
				do {
					sleep(1);
				} while(gardenLobbyArea.contains(getMyPlayer().getLocation()));
			} else if(bankingArea.contains(getMyPlayer().getLocation()) && !bank.isOpen()) {
				if(calc.distanceTo(npcs.getNearest(fremennikBankerNPCID)) > 5) {
					walking.walkPathMM(bankingPath);
				} else {
					bank.open();
				}
			} else if(bankingArea.contains(getMyPlayer().getLocation()) && bank.isOpen()) {
				bank.depositAll();
				bank.close();
				
				equipment.getItem(Equipment.WEAPON).doAction("Sorceress's Garden");
			}
		} else {
			if(gardenLobbyArea.contains(getMyPlayer().getLocation())) {
				if(objects.getNearest(gardenGateObjectID).isOnScreen()) {
					objects.getNearest(gardenGateObjectID).doClick();
					return random(2600, 3000);
				} else {
					walking.walkTileMM(objects.getNearest(gardenGateObjectID).getLocation());
					return random(2600, 3000);
				}
			} else if(gardenSqirkTree.contains(getMyPlayer().getLocation())) {
				if(objects.getNearest(gardenSqirkTreeObjectID) != null) {
					log("in garden sqirk...");
					int timeout = 0;
					havePickedGardenFruit = false;
					do {
						hoverMouseOverTile(objects.getNearest(gardenSqirkTreeObjectID).getLocation());
						objects.getNearest(gardenSqirkTreeObjectID).doClick();
						sleep(1000);
						if(getMyPlayer().getAnimation() > 0 || getMyPlayer().isMoving()) {
							int secondTimeout = 0;
							do {
								sleep(1000);
								secondTimeout++;
							} while(!havePickedGardenFruit && !gardenLobbyArea.contains(getMyPlayer().getLocation()) && secondTimeout < 5);
						}
						timeout++;
					} while(timeout < 5 && !havePickedGardenFruit && !gardenLobbyArea.contains(getMyPlayer().getLocation()));
					
					if(havePickedGardenFruit) {
						do {
							sleep(1000);
						} while(!gardenLobbyArea.contains(getMyPlayer().getLocation()));
					}
				}
			} else {
				nowPlaying = garden.getCurrentAct();
				
				if(nowPlaying != null) {
					if(nowPlaying.actor.isActorAtStart()) {
						if(nowPlaying.actor.cameraAngleMin >= 0)
							if(camera.getAngle() < nowPlaying.actor.cameraAngleMin || camera.getAngle() > nowPlaying.actor.cameraAngleMax)
								camera.setAngle(random(nowPlaying.actor.cameraAngleMin, nowPlaying.actor.cameraAngleMax));
						
						hoverMouseOverTile(nowPlaying.actor.actorEndTile);
						if(nowPlaying.isSafeForActorToMove()) {
							walking.walkTileOnScreen(nowPlaying.actor.actorEndTile);
							return random(1700, 2500);
						}
					}
				}
			}
		}
		
		return 1;
	}
	
	@Override
	public void messageReceived(MessageEvent e) {
		if(e.getMessage().contains("emanating")) {
			havePickedGardenFruit = true;
			accumulatedFruit++;
		} else if(e.getMessage().contains("teleported")) {
			accumulatedFail++;
		}
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
		if(game.isWelcomeScreen() || !game.isLoggedIn()) return ;

		Graphics2D g = (Graphics2D)g2;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if(!isScriptLoaded) {
			Scoreboard loadingBoard = new Scoreboard(Scoreboard.BOTTOM_RIGHT, 128, 5);
			loadingBoard.addWidget(new ScoreboardWidget(clockImage, "Loading..."));
			loadingBoard.drawScoreboard(g);

			return ;
		}

		//Draw Custom Mouse Cursor
		g.drawImage(cursorImage, mouse.getLocation().x - 16, mouse.getLocation().y - 16, observer);

		//Draw Top Left Scoreboard
		topLeftScoreboard.getWidget(pickedFruitWidgetIndex).setWidgetText(numberFormatter.format(accumulatedFruit));
		topLeftScoreboard.getWidget(failedMazeWidgetIndex).setWidgetText(numberFormatter.format(accumulatedFail));
		topLeftScoreboard.drawScoreboard(g);

		//Draw Top Right Scoreboard
		topRightScoreboard.getWidget(currentRuntimeWidgetIndex).setWidgetText(millisToClock(System.currentTimeMillis() - startingTime));
		topRightScoreboard.getWidget(farmingXPEarnedWidgetIndex).setWidgetText(numberFormatter.format(skills.getCurrentExp(Skills.FARMING) - startingFarmingXP));
		topRightScoreboard.getWidget(theivingXPEarnedWidgetIndex).setWidgetText("\u2248" + numberFormatter.format(Math.floor(accumulatedFruit / 3) * 2350));
		topRightScoreboard.drawScoreboard(g);
		
		g.setColor(Color.black);
		g.drawString(camera.getAngle() + "", 256, 256);
		g.setColor(Color.blue);
		g.drawString(camera.getAngle() + "", 255, 255);
	}
	
	@Override
	public void onFinish() {
		return ;
	}
	
	/**
	 * Formats millisecond time into HH:MM:SS
	 * 
	 * @param milliseconds				milliseconds that should be converted into
	 * 									the HH:MM:SS format
	 * 									@see java.lang.System
	 * @return							formatted HH:MM:SS string
	 * @since 0.1
	 */
	private String millisToClock(long milliseconds) {
		long seconds = (milliseconds / 1000), minutes = 0, hours = 0;

		if (seconds >= 60) {
			minutes = (seconds / 60);
			seconds -= (minutes * 60);
		}

		if (minutes >= 60) {
			hours = (minutes / 60);
			minutes -= (hours * 60);
		}

		return (hours < 10 ? "0" + hours + ":" : hours + ":")
				+ (minutes < 10 ? "0" + minutes + ":" : minutes + ":")
				+ (seconds < 10 ? "0" + seconds : seconds);
	}
	
	/**
	 * Scoreboard is a class for assembling individual scoreboards with widgets
	 * in a canvas space.
	 *
	 * @author allometry
	 * @version 1.0
	 * @since 1.0
	 */
	public class Scoreboard {
		public static final int TOP_LEFT = 1, TOP_RIGHT = 2, BOTTOM_LEFT = 3, BOTTOM_RIGHT = 4;
		public static final int gameCanvasTop = 25, gameCanvasLeft = 25, gameCanvasBottom = 309, gameCanvasRight = 487;

		private ImageObserver observer = null;

		private int scoreboardLocation, scoreboardX, scoreboardY, scoreboardWidth,
				scoreboardHeight, scoreboardArc;

		private ArrayList<ScoreboardWidget> widgets = new ArrayList<ScoreboardWidget>();

		/**
		 * Creates a new instance of Scoreboard.
		 *
		 * @param scoreboardLocation	the location of where the scoreboard should be drawn on the screen
		 * 								@see Scoreboard.TOP_LEFT
		 * 								@see Scoreboard.TOP_RIGHT
		 * 								@see Scoreboard.BOTTOM_LEFT
		 * 								@see Scoreboard.BOTTOM_RIGHT
		 * @param width					the pixel width of the scoreboard
		 * @param arc					the pixel arc of the scoreboard rounded rectangle
		 * @since 1.0
		 */
		public Scoreboard(int scoreboardLocation, int width, int arc) {
			this.scoreboardLocation = scoreboardLocation;
			scoreboardHeight = 10;
			scoreboardWidth = width;
			scoreboardArc = arc;

			switch (scoreboardLocation) {
			case 1:
				scoreboardX = gameCanvasLeft;
				scoreboardY = gameCanvasTop;
				break;

			case 2:
				scoreboardX = gameCanvasRight - scoreboardWidth;
				scoreboardY = gameCanvasTop;
				break;

			case 3:
				scoreboardX = gameCanvasLeft;
				break;

			case 4:
				scoreboardX = gameCanvasRight - scoreboardWidth;
				break;
			}
		}

		/**
		 * Adds a ScoreboardWidget to the Scoreboard.
		 *
		 * @param widget				an instance of a ScoreboardWidget containing an image
		 * 								and text
		 * 								@see ScoreboardWidget
		 * @return						true if the widget was added to Scoreboard
		 * @since 1.0
		 */
		public boolean addWidget(ScoreboardWidget widget) {
			return widgets.add(widget);
		}

		/**
		 * Gets a ScoreboardWidget by it's index within Scoreboard.
		 *
		 * @param widgetIndex			the index of the ScoreboardWidget
		 * @return						an instance of ScoreboardWidget
		 * @since 1.0
		 */
		public ScoreboardWidget getWidget(int widgetIndex) {
			try {
				return widgets.get(widgetIndex);
			} catch (Exception e) {
				log.warning("Warning: " + e.getMessage());
				return null;
			}
		}

		/**
		 * Gets the Scoreboard widgets.
		 *
		 * @return						an ArrayList filled with ScoreboardWidget's
		 */
		public ArrayList<ScoreboardWidget> getWidgets() {
			return widgets;
		}

		/**
		 * Draws the Scoreboard and ScoreboardWidget's to an instances of Graphics2D.
		 *
		 * @param g						an instance of Graphics2D
		 * @return						true if Scoreboard was able to draw to the Graphics2D instance and false if it wasn't
		 * @since 1.0
		 */
		public boolean drawScoreboard(Graphics2D g) {
			try {
				if(scoreboardHeight <= 10) {
					for (ScoreboardWidget widget : widgets) {
						scoreboardHeight += widget.getWidgetImage().getHeight(observer) + 4;
					}
				}

				if (scoreboardLocation == 3 || scoreboardLocation == 4) {
					scoreboardY = gameCanvasBottom - scoreboardHeight;
				}

				RoundRectangle2D scoreboard = new RoundRectangle2D.Float(
						scoreboardX, scoreboardY, scoreboardWidth,
						scoreboardHeight, scoreboardArc, scoreboardArc);

				g.setColor(new Color(0, 0, 0, 127));
				g.fill(scoreboard);

				int x = scoreboardX + 5;
				int y = scoreboardY + 5;
				for (ScoreboardWidget widget : widgets) {
					widget.drawWidget(g, x, y);
					y += widget.getWidgetImage().getHeight(observer) + 4;
				}

				return true;
			} catch (Exception e) {
				return false;
			}
		}

		/**
		 * Returns the height of the Scoreboard with respect to it's contained ScoreboardWidget's.
		 *
		 * @return						the pixel height of the Scoreboard
		 * @since 1.0
		 */
		public int getHeight() {
			return scoreboardHeight;
		}
	}

	/**
	 * ScoreboardWidget is a container intended for use with a Scoreboard. Scoreboards contain
	 * an image and text, which are later drawn to an instance of Graphics2D.
	 *
	 * @author allometry
	 * @version 1.0
	 * @since 1.0
	 * @see Scoreboard
	 */
	public class ScoreboardWidget {
		private ImageObserver observer = null;
		private Image widgetImage;
		private String widgetText;

		/**
		 * Creates a new instance of ScoreboardWidget.
		 *
		 * @param widgetImage			an instance of an Image. Recommended size is 16x16 pixels
		 * 								@see java.awt.Image
		 * @param widgetText			text to be shown on the right of the widgetImage
		 * @since 1.0
		 */
		public ScoreboardWidget(Image widgetImage, String widgetText) {
			this.widgetImage = widgetImage;
			this.widgetText = widgetText;
		}

		/**
		 * Gets the widget image.
		 *
		 * @return						the Image of ScoreboardWidget
		 * 								@see java.awt.Image
		 * @since 1.0
		 */
		public Image getWidgetImage() {
			return widgetImage;
		}

		/**
		 * Sets the widget image.
		 *
		 * @param widgetImage			an instance of an Image. Recommended size is 16x16 pixels
		 * 								@see java.awt.Image
		 * @since 1.0
		 */
		public void setWidgetImage(Image widgetImage) {
			this.widgetImage = widgetImage;
		}

		/**
		 * Gets the widget text.
		 *
		 * @return						the text of ScoreboardWidget
		 * @since 1.0
		 */
		public String getWidgetText() {
			return widgetText;
		}

		/**
		 * Sets the widget text.
		 *
		 * @param widgetText			text to be shown on the right of the widgetImage
		 * @since 1.0
		 */
		public void setWidgetText(String widgetText) {
			this.widgetText = widgetText;
		}

		/**
		 * Draws the ScoreboardWidget to an instance of Graphics2D.
		 *
		 * @param g						an instance of Graphics2D
		 * @param x						horizontal pixel location of where to draw the widget
		 * @param y						vertical pixel location of where to draw the widget
		 * @since 1.0
		 */
		public void drawWidget(Graphics2D g, int x, int y) {
			g.setColor(Color.white);
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

			g.drawImage(widgetImage, x, y, observer);
			g.drawString(widgetText, x + widgetImage.getWidth(observer) + 4, y + 12);
		}
	}
}
