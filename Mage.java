import java.awt.Color;

public class Mage extends GameCharacter {
	public Mage(String name, int health) {
		super(name, health);
	}

	@Override
	public String attack(GameCharacter target) {
		int dmg = 20;
		target.takeDamage(dmg);
		return getName() + " uses " + getAttackName() + " on " + target.getName() + " for " + dmg + " damage!";
	}

	@Override
	public String specialMove(GameCharacter target) {
		int dmg = 40;
		target.takeDamage(dmg);
		return getName() + " casts " + getSpecialName() + " on " + target.getName() + " for " + dmg + " damage!";
	}

	@Override
	public String getType() { return "Mage"; }

	@Override
	public String getAttackName() { return "Chrono Dash"; }

	@Override
	public String getSpecialName() { return "Zaman Force"; }

	@Override
	public String getDescription() { return "High damage caster. Attack: 20 dmg. Special: 40 dmg."; }

	@Override
	public Color getColor() { return new Color(0x7C, 0x4D, 0xFF); }
}
