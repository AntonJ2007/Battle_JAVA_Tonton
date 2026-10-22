import java.awt.Color;
import java.util.Random;

public class Archer extends GameCharacter {
	private static final Random RNG = new Random();

	public Archer(String name, int health) {
		super(name, health);
	}

	@Override
	public String attack(GameCharacter target) {
		int dmg = 18;
		boolean crit = RNG.nextInt(100) < 20;
		if (crit) {
			dmg *= 2;
		}
		target.takeDamage(dmg);
		return getName() + " fires " + getAttackName() + " at " + target.getName() + " for " + dmg
				+ (crit ? " damage (CRITICAL!)" : " damage!");
	}

	@Override
	public String specialMove(GameCharacter target) {
		int total = 0;
		for (int i = 0; i < 3; i++) {
			total += 10 + RNG.nextInt(5); // 10-14 per arrow
		}
		target.takeDamage(total);
		return getName() + " calls down " + getSpecialName() + " on " + target.getName() + " for " + total
				+ " damage (3 arrows)!";
	}

	@Override
	public String getType() { return "Archer"; }

	@Override
	public String getAttackName() { return "Piercing Shot"; }

	@Override
	public String getSpecialName() { return "Arrow Rain"; }

	@Override
	public String getDescription() { return "20% crit chance. Attack: 18 dmg. Special: 3 arrows, ~36 dmg."; }

	@Override
	public Color getColor() { return new Color(0x2E, 0xA0, 0x43); }
}
