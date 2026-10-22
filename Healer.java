import java.awt.Color;

public class Healer extends GameCharacter {
	public Healer(String name, int health) {
		super(name, health);
	}

	@Override
	public String attack(GameCharacter target) {
		int dmg = 12;
		target.takeDamage(dmg);
		return getName() + " strikes with " + getAttackName() + " on " + target.getName() + " for " + dmg
				+ " damage!";
	}

	@Override
	public String specialMove(GameCharacter target) {
		int dmg = 15;
		int heal = 30;
		target.takeDamage(dmg);
		heal(heal);
		return getName() + " channels " + getSpecialName() + ": " + dmg + " damage to " + target.getName()
				+ " and heals " + heal + " HP!";
	}

	@Override
	public String getType() { return "Healer"; }

	@Override
	public String getAttackName() { return "Holy Smite"; }

	@Override
	public String getSpecialName() { return "Divine Light"; }

	@Override
	public String getDescription() { return "Sustain fighter. Attack: 12 dmg. Special: 15 dmg + heal 30."; }

	@Override
	public Color getColor() { return new Color(0xF9, 0xA8, 0x25); }
}
