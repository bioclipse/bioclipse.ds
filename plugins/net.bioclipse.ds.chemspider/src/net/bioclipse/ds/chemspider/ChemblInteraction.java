package net.bioclipse.ds.chemspider;

public class ChemblInteraction {
	
	String title;
	String value;
	String unit;
	String targetType;
	String interactionType;
	String relation;
	String description;

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public String getInteractionType() {
		return interactionType;
	}
	public void setInteractionType(String interactionType) {
		this.interactionType = interactionType;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ChemblInteraction(String value, String unit, String interactionType) {
		super();
		this.value = value;
		this.unit = unit;
		this.interactionType = interactionType;
	}
	@Override
	public String toString() {
		return "ChemblInteraction [title=" + title + ", value=" + value
				+ ", unit=" + unit + ", targetType=" + targetType
				+ ", interactionType=" + interactionType + ", relation="
				+ relation + ", description=" + description + "]";
	}

	
}
