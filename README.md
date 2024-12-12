# C2C-GEE
## A google earth engine implementation of the C2C change detection and change metrics calculation algorithm

saverio.francini@unifi.it

This folder is an eclipse project:

- (1) the "data/input.csv" file is the input of the java code. Once on GEE, the input should be an image collection
- (2) the "data" folder includes the outputs of the java code.
- (3) the "src" folder includes the java codes. They are divided into four packages:
	  - (1) "com" -> is the "com.google.earthengine.api.base" package and was used to define input parameters, documentation and default values.
	  - (2) "objects" -> includes the java classes where "objects" (e.g. points, segments and timelines) are defined. 
	  - (3) "functions" -> includes the java classes that perform the procedure. It also includes the Main.java class where the c2cBottomUp method is defined (the method that should be available for users on GEE). The Main.java class also includes input parameters documentation and default values (defined using the com.google.earthengine.api.base.ArgsBase class)  
- (4) "run" -> includes the wrapper that:
						  - (i) reads the data/input.csv file, 
						  - (ii) applies the c2cBottomUp method on each time series in the input file
						  - (iii) writes the data/output_c2c.csv files (one for each time line in the data/input.csv file)

For each pixel and for each year we calculated seven values:
index, magnitude, duration, rate, postMagnitude, postDuration and postRate.
Thus, we may want that the output will be an image collection (or array image)
with seven bands (one for each value we calculated in the output csv files).
