import Image
import ImageEnhance
import ImageOps
import ImageStat
import numpy
from pImgMgr import pImgMgr

class pCell(object):

    """Define enumeration for defining types of cells"""
    BOARD = 0
    STROKE = 1
    FOREGROUND = 2
    UNCLASSIFIED = 3
    

    """Cell width and height are constant for processing job so make static"""
    width = 0
    height = 0

    
    """Define constructor for cell giving x and y of top left corner"""
    def __init__(self, x, y, im):
        self.x = x
        self.y = y
        self.im = im # pImgMgr
        self.celltype = pCell.UNCLASSIFIED
        self.boundaries = (self.x, self.y, self.x + pCell.width, self.y + pCell.height)
        self.I = None
        self.sig = None


    """Static method for setting height and width"""
    @staticmethod
    def setHeightWidth(h, w):
        pCell.height = h
        pCell.width = w

    
    # This is where the math goes COLIN
    """Tell cell to classify itself"""
    def classify(self, Iw):
        Tw = 2    # Lower gives more foreground
        Tsig = 40 # Higher gives more Board

        if self.I == None:  
            cell_bwimage = self.im.getBW().crop(self.boundaries)
            I = ImageStat.Stat(cell_bwimage).mean[0]
            sig = ImageStat.Stat(cell_bwimage).stddev[0]
        else:
            I = self.I
            sig = self.sig

        sigw = 0.1  #Seems to work for this value
        SFactor = 0.8
        
        if I > (Iw * SFactor)  and sig/sigw < Tsig:
            self.celltype = pCell.BOARD
            #print "BOARD CELL"

        elif I > (Iw * SFactor) and sig/sigw >= Tsig:
            self.celltype = pCell.STROKE
            #print "STROKE CELL"

        else:
            self.celltype = pCell.FOREGROUND

        return self.celltype

    """Show the cell on screen (probably for debugging"""
    def show(self):
        im = self.im.getColor()
        region = self.boundaries
        im.crop(region).show()

    def cellData(self):
        im = self.im.getColor()
        return im.crop(self.boundaries)

    def histogram(self):
        data = self.im.getBW().crop(self.boundaries)

        self.I = ImageStat.Stat(data).mean[0]
        self.sig = ImageStat.Stat(data).stddev[0]

        lum_hist = data.histogram()
        return numpy.array(lum_hist)

    def makeWhite(self):
        #makeWhiteCell = ImageEnhance.Brightness(self.cells[x][y].cellData())
        #makeWhiteCell = makeWhiteCell.enhance(100)
        makeWhiteCell = self.cellData().convert('L')
        lut = [255 if v < 254 else 0 for v in range(256)]
        makeWhiteCell = makeWhiteCell.point(lut, '1')
        #self.im.getColor().paste(makeWhiteCell, self.cells[x][y].boundaries)
        return makeWhiteCell

    def make2D(self):
        make2D = self.cellData().convert('L')
        color = self.cellData()
        stat = ImageStat.Stat(make2D)
        pixels = make2D.load()
        colPix = color.load()
        width = pCell.width
        height = pCell.height
        for xx in range(width):
            for yy in range(height):
                cpixel = colPix[xx, yy]
                if round(sum(cpixel)/float(len(cpixel))) > stat.mean[0]:
                    color.putpixel((xx, yy), (255, 255, 255))
        make2D = make2D.point(lambda i: i > (stat.mean[0]-stat.stddev[0]/5) and 255)

        #self.im.getColor().paste(color, self.cells[x][y].boundaries)
        return color
