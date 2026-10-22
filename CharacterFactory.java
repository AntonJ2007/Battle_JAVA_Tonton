/** Creates the correct hero from its type name. */
public final class CharacterFactory {

	public static final String[] TYPES = { "Warrior", "Mage", "Archer", "Healer" };

	public static GameCharacter create(String type, String name, int hp) {
		switch (type) {
		case "Warrior": return new Warrior(name, hp);
		case "Mage":    return new Mage(name, hp);
		case "Archer":  return new Archer(name, hp);
		default:        return new Healer(name, hp);
		}
	}
}
