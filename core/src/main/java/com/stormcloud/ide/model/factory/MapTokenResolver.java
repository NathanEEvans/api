package com.stormcloud.ide.model.factory;

import com.stormcloud.ide.model.factory.ITokenResolver;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author martijn
 */
public class MapTokenResolver implements ITokenResolver {

    private Map<String, String> tokenMap = new HashMap<String, String>(0);

    /**
     *
     * @param tokenMap
     */
    public MapTokenResolver(Map<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    /**
     *
     * @param tokenName
     * @return
     */
    @Override
    public String resolveToken(String tokenName) {
        return this.tokenMap.get(tokenName);
    }
}