from pCell import *
import pImage
from pImageSequence import pImageSequence
import time
import os

CELLS_PER_ROW = 96
CELLS_PER_COLUMN = 54
imageDir = "./test_images/"

start = time.time()
    
print "Creating pImageSequence"

imageSeq = pImageSequence(imageDir, CELLS_PER_ROW, CELLS_PER_COLUMN)

print "Classifying cells"
#imageSeq.generateLuminance()
imageSeq.classifyCells()

imageSeq.findKeyImages()

end = time.time()

print "Time elapsed %s" % (end -start)
os.system("rm %s/*.jpgout" % imageDir)



