package cz.jeme.programu.latestdeaths;

import java.util.Date;

public class Death {
	private String name;
	private String dimension;
	private int xPos;
	private int yPos;
	private int zPos;
	private String deathCause;
	private String killerEntity;
	private String shooterEntity;
	private Date date;

	public Death(String name, String dimension, int xPos, int yPos, int zPos, String deathCause, String killerEntity,
			String shooterEntity, Date date) {
		super();
		this.name = name;
		this.dimension = dimension;
		this.xPos = xPos;
		this.yPos = yPos;
		this.zPos = zPos;
		this.deathCause = deathCause;
		this.killerEntity = killerEntity;
		this.shooterEntity = shooterEntity;
		this.date = date;
	}

	@Override
	public String toString() {
		return "Death [name=" + name + ", dimension=" + dimension + ", xPos=" + xPos + ", yPos=" + yPos + ", zPos="
				+ zPos + ", deathCause=" + deathCause + ", killerEntity=" + killerEntity + ", shooterEntity="
				+ shooterEntity + ", date=" + date + "]";
	}

	public String getName() {
		return name;
	}

	public String getDimension() {
		return dimension;
	}

	public int getxPos() {
		return xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public int getzPos() {
		return zPos;
	}

	public String getDeathCause() {
		return deathCause;
	}

	public String getKillerEntity() {
		return killerEntity;
	}

	public String getShooterEntity() {
		return shooterEntity;
	}

	public Date getDate() {
		return date;
	}

}
