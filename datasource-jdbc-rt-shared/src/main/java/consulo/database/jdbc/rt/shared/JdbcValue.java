/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package consulo.database.jdbc.rt.shared;

import org.apache.thrift.EncodingUtils;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;
import org.apache.thrift.scheme.TupleScheme;

import java.util.*;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
public class JdbcValue implements org.apache.thrift.TBase<JdbcValue, JdbcValue._Fields>, java.io.Serializable, Cloneable, Comparable<JdbcValue> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("JdbcValue");

  private static final org.apache.thrift.protocol.TField TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("type", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField INT_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("intValue", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField STRING_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("stringValue", org.apache.thrift.protocol.TType.STRING, (short)3);
  private static final org.apache.thrift.protocol.TField BOOL_VALUE_FIELD_DESC = new org.apache.thrift.protocol.TField("boolValue", org.apache.thrift.protocol.TType.BOOL, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new JdbcValueStandardSchemeFactory());
    schemes.put(TupleScheme.class, new JdbcValueTupleSchemeFactory());
  }

  /**
   * 
   * @see JdbcValueType
   */
  public JdbcValueType type; // required
  public int intValue; // optional
  public String stringValue; // optional
  public boolean boolValue; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    /**
     * 
     * @see JdbcValueType
     */
    TYPE((short)1, "type"),
    INT_VALUE((short)2, "intValue"),
    STRING_VALUE((short)3, "stringValue"),
    BOOL_VALUE((short)4, "boolValue");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // TYPE
          return TYPE;
        case 2: // INT_VALUE
          return INT_VALUE;
        case 3: // STRING_VALUE
          return STRING_VALUE;
        case 4: // BOOL_VALUE
          return BOOL_VALUE;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __INTVALUE_ISSET_ID = 0;
  private static final int __BOOLVALUE_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.INT_VALUE,_Fields.STRING_VALUE,_Fields.BOOL_VALUE};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TYPE, new org.apache.thrift.meta_data.FieldMetaData("type", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, JdbcValueType.class)));
    tmpMap.put(_Fields.INT_VALUE, new org.apache.thrift.meta_data.FieldMetaData("intValue", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.STRING_VALUE, new org.apache.thrift.meta_data.FieldMetaData("stringValue", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.BOOL_VALUE, new org.apache.thrift.meta_data.FieldMetaData("boolValue", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(JdbcValue.class, metaDataMap);
  }

  public JdbcValue() {
  }

  public JdbcValue(
    JdbcValueType type)
  {
    this();
    this.type = type;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public JdbcValue(JdbcValue other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetType()) {
      this.type = other.type;
    }
    this.intValue = other.intValue;
    if (other.isSetStringValue()) {
      this.stringValue = other.stringValue;
    }
    this.boolValue = other.boolValue;
  }

  public JdbcValue deepCopy() {
    return new JdbcValue(this);
  }

  @Override
  public void clear() {
    this.type = null;
    setIntValueIsSet(false);
    this.intValue = 0;
    this.stringValue = null;
    setBoolValueIsSet(false);
    this.boolValue = false;
  }

  /**
   * 
   * @see JdbcValueType
   */
  public JdbcValueType getType() {
    return this.type;
  }

  /**
   * 
   * @see JdbcValueType
   */
  public JdbcValue setType(JdbcValueType type) {
    this.type = type;
    return this;
  }

  public void unsetType() {
    this.type = null;
  }

  /** Returns true if field type is set (has been assigned a value) and false otherwise */
  public boolean isSetType() {
    return this.type != null;
  }

  public void setTypeIsSet(boolean value) {
    if (!value) {
      this.type = null;
    }
  }

  public int getIntValue() {
    return this.intValue;
  }

  public JdbcValue setIntValue(int intValue) {
    this.intValue = intValue;
    setIntValueIsSet(true);
    return this;
  }

  public void unsetIntValue() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __INTVALUE_ISSET_ID);
  }

  /** Returns true if field intValue is set (has been assigned a value) and false otherwise */
  public boolean isSetIntValue() {
    return EncodingUtils.testBit(__isset_bitfield, __INTVALUE_ISSET_ID);
  }

  public void setIntValueIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __INTVALUE_ISSET_ID, value);
  }

  public String getStringValue() {
    return this.stringValue;
  }

  public JdbcValue setStringValue(String stringValue) {
    this.stringValue = stringValue;
    return this;
  }

  public void unsetStringValue() {
    this.stringValue = null;
  }

  /** Returns true if field stringValue is set (has been assigned a value) and false otherwise */
  public boolean isSetStringValue() {
    return this.stringValue != null;
  }

  public void setStringValueIsSet(boolean value) {
    if (!value) {
      this.stringValue = null;
    }
  }

  public boolean isBoolValue() {
    return this.boolValue;
  }

  public JdbcValue setBoolValue(boolean boolValue) {
    this.boolValue = boolValue;
    setBoolValueIsSet(true);
    return this;
  }

  public void unsetBoolValue() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __BOOLVALUE_ISSET_ID);
  }

  /** Returns true if field boolValue is set (has been assigned a value) and false otherwise */
  public boolean isSetBoolValue() {
    return EncodingUtils.testBit(__isset_bitfield, __BOOLVALUE_ISSET_ID);
  }

  public void setBoolValueIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __BOOLVALUE_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case TYPE:
      if (value == null) {
        unsetType();
      } else {
        setType((JdbcValueType)value);
      }
      break;

    case INT_VALUE:
      if (value == null) {
        unsetIntValue();
      } else {
        setIntValue((Integer)value);
      }
      break;

    case STRING_VALUE:
      if (value == null) {
        unsetStringValue();
      } else {
        setStringValue((String)value);
      }
      break;

    case BOOL_VALUE:
      if (value == null) {
        unsetBoolValue();
      } else {
        setBoolValue((Boolean)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case TYPE:
      return getType();

    case INT_VALUE:
      return getIntValue();

    case STRING_VALUE:
      return getStringValue();

    case BOOL_VALUE:
      return isBoolValue();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case TYPE:
      return isSetType();
    case INT_VALUE:
      return isSetIntValue();
    case STRING_VALUE:
      return isSetStringValue();
    case BOOL_VALUE:
      return isSetBoolValue();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof JdbcValue)
      return this.equals((JdbcValue)that);
    return false;
  }

  public boolean equals(JdbcValue that) {
    if (that == null)
      return false;

    boolean this_present_type = true && this.isSetType();
    boolean that_present_type = true && that.isSetType();
    if (this_present_type || that_present_type) {
      if (!(this_present_type && that_present_type))
        return false;
      if (!this.type.equals(that.type))
        return false;
    }

    boolean this_present_intValue = true && this.isSetIntValue();
    boolean that_present_intValue = true && that.isSetIntValue();
    if (this_present_intValue || that_present_intValue) {
      if (!(this_present_intValue && that_present_intValue))
        return false;
      if (this.intValue != that.intValue)
        return false;
    }

    boolean this_present_stringValue = true && this.isSetStringValue();
    boolean that_present_stringValue = true && that.isSetStringValue();
    if (this_present_stringValue || that_present_stringValue) {
      if (!(this_present_stringValue && that_present_stringValue))
        return false;
      if (!this.stringValue.equals(that.stringValue))
        return false;
    }

    boolean this_present_boolValue = true && this.isSetBoolValue();
    boolean that_present_boolValue = true && that.isSetBoolValue();
    if (this_present_boolValue || that_present_boolValue) {
      if (!(this_present_boolValue && that_present_boolValue))
        return false;
      if (this.boolValue != that.boolValue)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_type = true && (isSetType());
    list.add(present_type);
    if (present_type)
      list.add(type.getValue());

    boolean present_intValue = true && (isSetIntValue());
    list.add(present_intValue);
    if (present_intValue)
      list.add(intValue);

    boolean present_stringValue = true && (isSetStringValue());
    list.add(present_stringValue);
    if (present_stringValue)
      list.add(stringValue);

    boolean present_boolValue = true && (isSetBoolValue());
    list.add(present_boolValue);
    if (present_boolValue)
      list.add(boolValue);

    return list.hashCode();
  }

  @Override
  public int compareTo(JdbcValue other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetType()).compareTo(other.isSetType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.type, other.type);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIntValue()).compareTo(other.isSetIntValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIntValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.intValue, other.intValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStringValue()).compareTo(other.isSetStringValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStringValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.stringValue, other.stringValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetBoolValue()).compareTo(other.isSetBoolValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBoolValue()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.boolValue, other.boolValue);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("JdbcValue(");
    boolean first = true;

    sb.append("type:");
    if (this.type == null) {
      sb.append("null");
    } else {
      sb.append(this.type);
    }
    first = false;
    if (isSetIntValue()) {
      if (!first) sb.append(", ");
      sb.append("intValue:");
      sb.append(this.intValue);
      first = false;
    }
    if (isSetStringValue()) {
      if (!first) sb.append(", ");
      sb.append("stringValue:");
      if (this.stringValue == null) {
        sb.append("null");
      } else {
        sb.append(this.stringValue);
      }
      first = false;
    }
    if (isSetBoolValue()) {
      if (!first) sb.append(", ");
      sb.append("boolValue:");
      sb.append(this.boolValue);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class JdbcValueStandardSchemeFactory implements SchemeFactory {
    public JdbcValueStandardScheme getScheme() {
      return new JdbcValueStandardScheme();
    }
  }

  private static class JdbcValueStandardScheme extends StandardScheme<JdbcValue> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, JdbcValue struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.type = consulo.database.jdbc.rt.shared.JdbcValueType.findByValue(iprot.readI32());
              struct.setTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // INT_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.intValue = iprot.readI32();
              struct.setIntValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // STRING_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.stringValue = iprot.readString();
              struct.setStringValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // BOOL_VALUE
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.boolValue = iprot.readBool();
              struct.setBoolValueIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, JdbcValue struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.type != null) {
        oprot.writeFieldBegin(TYPE_FIELD_DESC);
        oprot.writeI32(struct.type.getValue());
        oprot.writeFieldEnd();
      }
      if (struct.isSetIntValue()) {
        oprot.writeFieldBegin(INT_VALUE_FIELD_DESC);
        oprot.writeI32(struct.intValue);
        oprot.writeFieldEnd();
      }
      if (struct.stringValue != null) {
        if (struct.isSetStringValue()) {
          oprot.writeFieldBegin(STRING_VALUE_FIELD_DESC);
          oprot.writeString(struct.stringValue);
          oprot.writeFieldEnd();
        }
      }
      if (struct.isSetBoolValue()) {
        oprot.writeFieldBegin(BOOL_VALUE_FIELD_DESC);
        oprot.writeBool(struct.boolValue);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class JdbcValueTupleSchemeFactory implements SchemeFactory {
    public JdbcValueTupleScheme getScheme() {
      return new JdbcValueTupleScheme();
    }
  }

  private static class JdbcValueTupleScheme extends TupleScheme<JdbcValue> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, JdbcValue struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetType()) {
        optionals.set(0);
      }
      if (struct.isSetIntValue()) {
        optionals.set(1);
      }
      if (struct.isSetStringValue()) {
        optionals.set(2);
      }
      if (struct.isSetBoolValue()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetType()) {
        oprot.writeI32(struct.type.getValue());
      }
      if (struct.isSetIntValue()) {
        oprot.writeI32(struct.intValue);
      }
      if (struct.isSetStringValue()) {
        oprot.writeString(struct.stringValue);
      }
      if (struct.isSetBoolValue()) {
        oprot.writeBool(struct.boolValue);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, JdbcValue struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.type = consulo.database.jdbc.rt.shared.JdbcValueType.findByValue(iprot.readI32());
        struct.setTypeIsSet(true);
      }
      if (incoming.get(1)) {
        struct.intValue = iprot.readI32();
        struct.setIntValueIsSet(true);
      }
      if (incoming.get(2)) {
        struct.stringValue = iprot.readString();
        struct.setStringValueIsSet(true);
      }
      if (incoming.get(3)) {
        struct.boolValue = iprot.readBool();
        struct.setBoolValueIsSet(true);
      }
    }
  }

}

