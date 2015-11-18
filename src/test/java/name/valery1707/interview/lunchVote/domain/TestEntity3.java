package name.valery1707.interview.lunchVote.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@SuppressWarnings("unused")
public class TestEntity3 extends ABaseEntity {
	@ManyToOne
	private TestEntity2 parent;

	@Column
	@NotNull
	@Size(min = 1, max = 1024)
	private String name;

	@Column
	@NotNull
	private byte primitiveByte;

	@Column
	@NotNull
	private short primitiveShort;

	@Column
	@NotNull
	private int primitiveInt;

	@Column
	@NotNull
	private long primitiveLong;

//	@Column
//	@NotNull
//	private float primitiveFloat;

	@Column
	@NotNull
	private double primitiveDouble;

	@Column
	@NotNull
	private boolean primitiveBoolean;

	@Column
	private Byte objectByte;

	@Column
	private Short objectShort;

	@Column
	private Integer objectInt;

	@Column
	private Long objectLong;

//	@Column
//	private Float objectFloat;

	@Column
	private Double objectDouble;

	@Column
	private BigDecimal objectDecimal;

	@Column
	private Boolean objectBoolean;

	public TestEntity2 getParent() {
		return parent;
	}

	public void setParent(TestEntity2 parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getPrimitiveByte() {
		return primitiveByte;
	}

	public void setPrimitiveByte(byte primitiveByte) {
		this.primitiveByte = primitiveByte;
	}

	public short getPrimitiveShort() {
		return primitiveShort;
	}

	public void setPrimitiveShort(short primitiveShort) {
		this.primitiveShort = primitiveShort;
	}

	public int getPrimitiveInt() {
		return primitiveInt;
	}

	public void setPrimitiveInt(int primitiveInt) {
		this.primitiveInt = primitiveInt;
	}

	public long getPrimitiveLong() {
		return primitiveLong;
	}

	public void setPrimitiveLong(long primitiveLong) {
		this.primitiveLong = primitiveLong;
	}

//	public float getPrimitiveFloat() {
//		return primitiveFloat;
//	}
//
//	public void setPrimitiveFloat(float primitiveFloat) {
//		this.primitiveFloat = primitiveFloat;
//	}

	public double getPrimitiveDouble() {
		return primitiveDouble;
	}

	public void setPrimitiveDouble(double primitiveDouble) {
		this.primitiveDouble = primitiveDouble;
	}

	public boolean isPrimitiveBoolean() {
		return primitiveBoolean;
	}

	public void setPrimitiveBoolean(boolean primitiveBoolean) {
		this.primitiveBoolean = primitiveBoolean;
	}

	public Byte getObjectByte() {
		return objectByte;
	}

	public void setObjectByte(Byte objectByte) {
		this.objectByte = objectByte;
	}

	public Short getObjectShort() {
		return objectShort;
	}

	public void setObjectShort(Short objectShort) {
		this.objectShort = objectShort;
	}

	public Integer getObjectInt() {
		return objectInt;
	}

	public void setObjectInt(Integer objectInt) {
		this.objectInt = objectInt;
	}

	public Long getObjectLong() {
		return objectLong;
	}

	public void setObjectLong(Long objectLong) {
		this.objectLong = objectLong;
	}

//	public Float getObjectFloat() {
//		return objectFloat;
//	}
//
//	public void setObjectFloat(Float objectFloat) {
//		this.objectFloat = objectFloat;
//	}

	public Double getObjectDouble() {
		return objectDouble;
	}

	public void setObjectDouble(Double objectDouble) {
		this.objectDouble = objectDouble;
	}

	public BigDecimal getObjectDecimal() {
		return objectDecimal;
	}

	public void setObjectDecimal(BigDecimal objectDecimal) {
		this.objectDecimal = objectDecimal;
	}

	public Boolean getObjectBoolean() {
		return objectBoolean;
	}

	public void setObjectBoolean(Boolean objectBoolean) {
		this.objectBoolean = objectBoolean;
	}
}
