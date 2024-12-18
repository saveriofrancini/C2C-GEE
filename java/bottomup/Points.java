/*
 * Class defining "Points" over a time series. Points are defined by year and value
 */

package objects;

public class Points {

	private double date = 0.0;
	private double value = 0.0;

	public Points(double date, double value){
		this.date = date;
		this.value = value;
	}

	public Points(Points point) {
		this.date = point.date;
		this.value = point.value;
	}

	public double getDate(){
		return this.date;
	}

	public double getValue(){
		return this.value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}


}