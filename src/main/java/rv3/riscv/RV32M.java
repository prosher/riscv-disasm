package rv3.riscv;

public enum RV32M implements RiscvInstruction {

    MUL     (Type.R, 0b0000001_0000000000_000_00000_0110011),
    MULH    (Type.R, 0b0000001_0000000000_001_00000_0110011),
    MULSU   (Type.R, 0b0000001_0000000000_010_00000_0110011),
    MULHU   (Type.R, 0b0000001_0000000000_011_00000_0110011),
    DIV     (Type.R, 0b0000001_0000000000_100_00000_0110011),
    DIVU    (Type.R, 0b0000001_0000000000_101_00000_0110011),
    REM     (Type.R, 0b0000001_0000000000_110_00000_0110011),
    REMU    (Type.R, 0b0000001_0000000000_111_00000_0110011),
    ;
    private final Type type;
    private final int code;

    RV32M(final Type type, final int code) {
        this.type = type;
        this.code = code;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getCode() {
        return code;
    }
}
