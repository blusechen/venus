package com.meidusa.venus.validate.file;

import com.meidusa.venus.validate.chain.ValidatorChain;

/**
 * Parse a xml config file to a ValidatorChain
 * 
 * @author lichencheng.daisy
 * 
 */
public interface ValidationFileParser {
    ValidatorChain parseValidationConfigs(ValidationFileInfo is);

}
