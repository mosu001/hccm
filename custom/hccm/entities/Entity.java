package hccm.entities;

/**
 * 
 * @author Michael O'Sullivan
 * @version 0.0.1
 * @since 0.0.1
 * 
 */
public interface Entity {
	Entity getEntityType();
	void setEntityType(Entity type);

	public String getName();
}
