import java.awt.Color;

public class Warrior extends GameCharacter {
	public Warrior(String name, int health) {
		super(name, health);
	}

	@Override
	public String attack(GameCharacter target) {
		int dmg = 15;
		target.takeDamage(dmg);
		return getName() + " uses " + getAttackName() + " on " + target.getName() + " for " + dmg + " damage!";
	}

	@Override
	public String specialMove(GameCharacter target) {
		int dmg = 50;
		target.takeDamage(dmg);
		return getName() + " unleashes " + getSpecialName() + " on " + target.getName() + " for " + dmg + " damage!";
	}

	@Override
	public String getType() { return "Warrior"; }

	@Override
	public String getAttackName() { return "Triple Kick"; }

	@Override
	public String getSpecialName() { return "Way of the Dragon"; }

	@Override
	public String getDescription() { return "Steady melee fighter. Attack: 15 dmg. Special: 30 dmg."; }

	@Override
	public Color getColor() { return new Color(0xE5, 0x39, 0x35); }
}
