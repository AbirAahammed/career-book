package redbull.ecard.local.LogicLayerTest;

import org.junit.Test;

import androidmads.library.qrgenearator.QRGEncoder;
import redbull.ecard.LogicLayer.QRGenerator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class QRTest {
    @Test
    public void testGenerator(){
        QRGenerator q= new QRGenerator();
        QRGEncoder encoder=q.getQRGencoder("7BFJRQw4K6dVBoWADcji1yOQXQu2",20);
        assertNotNull(q.getBitmap(encoder));
        //shouldn't return anything since view missing
        assertNull(q.getPos(50));
    }
    @Test
    public void testScanner(){
        //really cant do any test here, since cannot mock a camera(look for QR comes with it)
        //The test is moved to an actual device, and tests by there.
    }
}
