package run.groundTruth;

class AutoCosts {
	double endInsert;
	double middleInsert;
	double beginInsert;
	
	double endDelete;
	double delete;
	double leftRight;
	
	double eqCost;
	
	public AutoCosts() {
		
	}
	
	public AutoCosts(AutoCosts other) {
		this.endDelete = other.endDelete;
		this.delete = other.delete;
		this.leftRight = other.leftRight;
		
		this.endInsert = other.endDelete;
		this.middleInsert = other.middleInsert;
		this.beginInsert = other.beginInsert;
		this.eqCost = other.eqCost;
	}
}
