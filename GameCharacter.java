import java.awt.Color;

public abstract class GameCharacter {
	public static final int SPECIAL_COOLDOWN = 2;

	private final String name;
	private int health;
	private final int maxHealth;
	private int specialCooldown;

	public GameCharacter(String name, int health) {
		this.name = name;
		this.health = health;
		this.maxHealth = health;
	}

	// ---- abstract behaviour every character type must define
	public abstract String attack(GameCharacter target);

	public abstract String specialMove(GameCharacter target);

	public abstract String getType();

	public abstract String getAttackName();

	public abstract String getSpecialName();

	public abstract String getDescription();

	public abstract Color getColor();

	// ---- shared behaviour
	public void showName() {
		System.out.println("Character: " + name);
	}

	/** Template method: wraps the special move with cooldown handling. */
	public final String performSpecial(GameCharacter target) {
		if (!specialReady()) {
			return name + "'s special move is not ready yet!";
		}
		String result = specialMove(target);
		specialCooldown = SPECIAL_COOLDOWN;
		return result;
	}

	public void takeDamage(int amount) {
		health = Math.max(0, health - amount);
	}

	public void heal(int amount) {
		health = Math.min(maxHealth, health + amount);
	}

	public boolean isAlive() {
		return health > 0;
	}

	public boolean specialReady() {
		return specialCooldown == 0;
	}

	public void tickCooldown() {
		if (specialCooldown > 0) {
			specialCooldown--;
		}
	}

	public void restoreFull() {
		health = maxHealth;
		specialCooldown = 0;
	}

	public void restoreState(int health, int cooldown) {
		this.health = health;
		this.specialCooldown = cooldown;
	}

	public String getName() {
		return name;
	}

	public int getHealth() {
		return health;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getSpecialCooldown() {
		return specialCooldown;
	}

	@Override
	public String toString() {
		return name + " (" + getType() + ")";
	}
}
