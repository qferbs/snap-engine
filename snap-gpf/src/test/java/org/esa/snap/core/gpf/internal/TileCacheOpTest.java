package org.esa.snap.core.gpf.internal;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.GPF;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

/**
 * @author Marco Peters
 */
public class TileCacheOpTest {

    @Test
    public void createProductTest() {
        Product sourceProduct = new Product("test", "type", 10, 10);
        sourceProduct.addBand(new Band("b1", ProductData.TYPE_INT8, 10, 10));
        HashMap<String, Object> params = new HashMap<>();
        params.put("cacheSize", 2048);
        Product cachedProduct = GPF.createProduct("TileCache", params, sourceProduct);
        assertNotNull(cachedProduct);
    }
}