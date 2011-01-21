import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;

@ScriptManifest(authors = { "Allometry" }, keywords = "Thieving Sourceress Garden", name = "A. Sorceress Garden", version = 0.1, description = "Start, all options are in GUI.")
public class ASorceressGarden extends Script implements PaintListener {
	private int[] autumnElementals = { 5533, 5534, 5535, 5536, 5537, 5538 };
	private int[] autumnElementalsSafeAngles = { 179, 270, 0, 89, 0, 0 };
	private int autumnGate = 21731;
	private int autumnSqirkTree = 21768;
	private int autumnSqirk = 10846;
	private String autumnSqirkAction = "Pick-fruit";
	
	private int fountain = 21764;
	private String fountainAction = "Drink-from";
	
	//Banking Area
	//3269,3161 to 3272,3173
	
	//Apprentice Staging Area
	//3318,3137 to 3324,3141
	private int apprenticeNPC = 5532;
	private String apprenticeAction = "Teleport";
	private int broomstick = 14057;
	private String broomstickAction = "Sorceress's Garden";
	
	private String success = "An elemental force emanating from the garden teleports you away.";
	
	//Staging Area
	//2903,5463 to 2920,5480
	
	//Player Start Position in Autumn Garden
	//2908, 5461
	
	//Template
	//Element is at, go to location
	
	//First Elemental Script
	//2905,5460 -> 2902,5461
	//2903,5460 -> 2901,5455
	
	//Second Elemental
	//2900,5453 -> 2901,5451
	
	//Second and Third Elemental
	//2900,5451 to 2900,5452(1) && 2899,5449 to 2902,5449(2) -> 2903,5450
	
	//Fourth and Fifth Elemental
	//2903,5454 to 2905,5453(1) && 2904,5457 to 2910,5457(2) -> 2908,5456
	
	//Sixth Element
	//2911,5455 to 2913,5455 -> {2909,5448 to 2917,5453}
	
	@Override
	public boolean onStart() {
		return true;
	}
	
	@Override
	public int loop() {
		// TODO Auto-generated method stub
		return 1;
	}
	
	public void onRepaint(final Graphics g2) {
		Graphics2D g = (Graphics2D)g2;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g.setColor(Color.black);
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		g.drawString(npcs.getNearest(5538).getLocation().toString(),15, 65);
	}
	
	@Override
	public void onFinish() {
		return ;
	}
}
