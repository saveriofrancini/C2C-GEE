saverio.francini@unifi.it

This folder is an eclipse project

(1) the "data/input.csv" file is the input of the java code. Once on GEE, the input should be an image collection
(2) the "data" folder includes the outputs of the java code, plus a comparison with the outputs of the original C2C non-GEE code (see data/readme.txt for more info).
(3) the "bin" folder includes the compiled version of the java codes in the "src" folder
(4) the "src" folder includes the java code. It is divided into four packages:
	(1) "com" -> is the "com.google.earthengine.api.base" package and was used to define input parameters, documentation and default values.
	(2) "objects" -> includes the java classes where "objects" (e.g. points, segments and timelines) are defined. 
	(3) "functions" -> includes the java classes that perform the procedure. 
	    Includes the Main.java class where the c2cBottomUp method is defined (the method that should be available for users on GEE).
            The Main.java class also includes input parameters documentation and default values 
            (defined using the com.google.earthengine.api.base.ArgsBase class)  
	(4) "run" -> includes the wrapper that:
						 (i) reads the data/input.csv file, 
						 (ii) applies the c2cBottomUp method on each time series in the input file
						 (iii) writes the data/output_c2c.csv files (one for each time line in the data/input.csv file)

The idea is that once ported into GEE, the input and output will be image collections (which are easier to work with than array images).
With image collection as the output of this code, then any user utilizing the GEE "qualityMosaic" function 
would be easily able to create the greatest/smallest magnitude, duration, rate, post magnitude, post duration, or post rate disturbance maps. 
Also, the qualityMocais function outputs a multiband image and for each pixel detected as a disturbance all information will be available 
(magnitude, duration, rate, post magnitude, post duration, or post rate disturbance)
For these reasons, our code includes a java class (FilterAndFillChangesTimeLine.java) 
that serves to provide noData values (defined as: double noData = Double.NaN;) for years do not detected as vertices.
We may want that once in GEE the NaN values will appear as masked pixels in each image of the output image collection.

However, Noel Gorelick mentioned us that the output has to be an an array image. This is ok for us!

For each pixel and for each year we calculated seven values:
index, magnitude, duration, rate, postMagnitude, postDuration and postRate.
Thus, we may want that the output will be an image collection (or array image)
with seven bands (one for each value we calculated in the output csv files).
