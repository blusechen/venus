package com.meidusa.venus.validate.file;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.meidusa.venus.validate.chain.ValidatorChain;
import com.meidusa.venus.validate.chain.VenusValidatorChain;
import com.meidusa.venus.validate.exception.ValidationRuntimeException;
import com.meidusa.venus.validate.util.ClassLoaderUtil;
import com.meidusa.venus.validate.util.dom.DomHelper;
import com.meidusa.venus.validate.validator.FieldValidatorSupport;
import com.meidusa.venus.validate.validator.Validator;
import com.meidusa.venus.validate.validator.ValidatorSupport;
import com.meidusa.venus.validate.validator.VisitorFieldValidator;

/**
 * @author lichencheng.daisy
 * @since 1.0.0-SNAPSHOT
 * 
 */
public class DefaultValidationFileParser implements ValidationFileParser {

    private FilePathGenerator pathGenerator;

    // Property name for beanutils doing populate
    private static String TYPE = "type";
    private static String FIELD_NAME = "fieldName";

    // Xml element in validator configuration file
    // private static String XML_ELEMENT_VALIDATORS = "validators";
    private static String XML_ELEMENT_FIELD = "field";
    private static String XML_ATTRIBUTE_NAME = "name";
    private static String XML_ELEMENT_FIELD_VALIDATOR = "field-validator";
    private static String XML_ATTRIBUTE_TYPE = "type";
    private static String XML_ELEMENT_PROPERTY = "property";
    private static String XML_ELEMENT_VALIDATOR = "validator";
    private static String XML_ATTRIBUTE_CLASS = "class";

    // Validator Definition file name
    private static String VALIDATION_DEFINITION_FILE = "validator-definition.xml";

    private Map<String, Class<?>> validatorDefinition = new HashMap<String, Class<?>>();

    public DefaultValidationFileParser() {
        super();
        this.initValidatorDefinition();
        pathGenerator = new DefaultFilePathGenerator();
    }

    static {

        // register beanutils Date Convert
        DateTimeConverter dateConverter = new DateConverter();
        dateConverter.setPatterns(new String[] { "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd" });
        ConvertUtils.register(dateConverter, Date.class);
    }

    @Override
    public ValidatorChain parseValidationConfigs(ValidationFileInfo info) {
        String path = this.pathGenerator.getConfigPath(info);

        ValidatorChain retChain = new VenusValidatorChain();
        // Document validationDoc = DomHelper.parse(is,
        // validationConfigsDtdMappings);
        InputStream is = ClassLoaderUtil.getResourceAsStream(path, this.getClass());
        if (is == null) {
            return retChain;
        }
        Document validationDoc = DomHelper.parse(is, null);
        Element rootElement = validationDoc.getDocumentElement();
        this.analyzeValidators(rootElement, retChain);
        List<Element> fieldElements = DomHelper.findChilds(rootElement, XML_ELEMENT_FIELD);
        for (Iterator<Element> iterator = fieldElements.iterator(); iterator.hasNext();) {
            Element element = (Element) iterator.next();
            this.analyzeField(element, retChain, info);

        }
        return retChain;
    }

    private void analyzeValidators(Element validatorElement, ValidatorChain retValidators) {
        List<Element> exprValidators = DomHelper.findChilds(validatorElement, XML_ELEMENT_VALIDATOR);
        for (Iterator<Element> iterator = exprValidators.iterator(); iterator.hasNext();) {
            retValidators.addValidator(this.analyzeValidator(iterator.next()));
        }
    }

    private void analyzeField(Element fieldElement, ValidatorChain retValidators, ValidationFileInfo info) {
        String fieldName = fieldElement.getAttribute(XML_ATTRIBUTE_NAME);

        for (Iterator<Element> iterator = DomHelper.findChilds(fieldElement, XML_ELEMENT_FIELD_VALIDATOR).iterator(); iterator.hasNext();) {
            Element fieldValidatorElement = iterator.next();
            retValidators.addValidator(analyzeFieldValidator(fieldValidatorElement, fieldName, info));

        }

    }

    private Validator analyzeValidator(Element validator) {
        Map<String, String> validatorConfigs = new HashMap<String, String>();
        // get validator type
        String typeAttributes = validator.getAttribute(XML_ATTRIBUTE_TYPE);
        // get basic validator info
        validatorConfigs.put(TYPE, typeAttributes);
        // get params
        List<Element> paramElements = DomHelper.findChilds(validator, XML_ELEMENT_PROPERTY);
        for (Element paramElement : paramElements) {
            String name = paramElement.getAttribute(XML_ATTRIBUTE_NAME);
            String value = paramElement.getTextContent();
            validatorConfigs.put(name, value);

        }
        // init the validator
        ValidatorSupport retValidator = null;
        Class<?> validatorClass = this.validatorDefinition.get(typeAttributes);
        try {
            retValidator = (ValidatorSupport) validatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new ValidationRuntimeException("can't instantiate validator" + validatorClass.toString(), e);
        } catch (IllegalAccessException e) {
            throw new ValidationRuntimeException("can't access validator class" + validatorClass.toString(), e);
        }
        try {
            BeanUtils.populate(retValidator, validatorConfigs);
        } catch (Exception e) {
            throw new ValidationRuntimeException("cannot init validator", e);
        }
        return retValidator;
    }

    private Validator analyzeFieldValidator(Element fieldElement, String fieldName, ValidationFileInfo info) {
        Map<String, String> validatorConfigs = new HashMap<String, String>();
        // get validator type
        String typeAttributes = fieldElement.getAttribute(XML_ATTRIBUTE_TYPE);
        // get basic validator info
        validatorConfigs.put(TYPE, typeAttributes);
        if (fieldName != null && fieldName.length() > 0) {
            validatorConfigs.put(FIELD_NAME, fieldName);
        }
        // get params
        List<Element> paramElements = DomHelper.findChilds(fieldElement, XML_ELEMENT_PROPERTY);
        for (Element paramElement : paramElements) {
            String name = paramElement.getAttribute(XML_ATTRIBUTE_NAME);
            String value = paramElement.getTextContent();
            validatorConfigs.put(name, value);

        }
        // init the validator
        FieldValidatorSupport retValidator = null;
        Class<?> validatorClass = this.validatorDefinition.get(typeAttributes);
        try {
            retValidator = (FieldValidatorSupport) validatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new ValidationRuntimeException("can't instantiate validator" + validatorClass.toString(), e);
        } catch (IllegalAccessException e) {
            throw new ValidationRuntimeException("can't access validator class" + validatorClass.toString(), e);
        }
        try {
            BeanUtils.populate(retValidator, validatorConfigs);
        } catch (Exception e) {
            throw new ValidationRuntimeException("cannot init validator", e);
        }
        if (retValidator instanceof VisitorFieldValidator) {
            String path = ((VisitorFieldValidator) retValidator).getPath();
            if (path == null || path.length() == 0) {
                info.addInner(fieldName);
                path = pathGenerator.getConfigPath(info);
            }
            ValidatorChain chain = this.parseValidationConfigs(info);
            ((VisitorFieldValidator) retValidator).setInternalValidatorChain(chain);
        }

        return retValidator;
    }

    private void initValidatorDefinition() {
        InputStream definitionInputStream = ClassLoaderUtil.getResourceAsStream(VALIDATION_DEFINITION_FILE, this.getClass());
        this.parseValidationDefinitions(definitionInputStream);

    }

    public void parseValidationDefinitions(InputStream is) {
        Document definitionDoc = DomHelper.parse(is, null);
        List<Element> definitions = DomHelper.findChilds(definitionDoc.getDocumentElement(), XML_ELEMENT_VALIDATOR);
        for (Element validatorElement : definitions) {
            String name = validatorElement.getAttribute(XML_ATTRIBUTE_NAME);
            String className = validatorElement.getAttribute(XML_ATTRIBUTE_CLASS);
            Class<?> validatorClass;
            try {
                validatorClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ValidationRuntimeException("Can't find class" + className, e);

            }
            this.validatorDefinition.put(name, validatorClass);
        }
    }

}
