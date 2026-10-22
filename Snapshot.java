public class Snapshot {
	final GameCharacter current;
	final int hpA, hpB, cdA, cdB, logLength;

	Snapshot(GameCharacter current, GameCharacter a, GameCharacter b, int logLength) {
		this.current = current;
		this.hpA = a.getHealth();
		this.hpB = b.getHealth();
		this.cdA = a.getSpecialCooldown();
		this.cdB = b.getSpecialCooldown();
		this.logLength = logLength;
	}
}
