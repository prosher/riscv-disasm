package rv3.riscv;

public enum RV32I implements RiscvInstruction {
    LUI     (Type.U, 0b0110111),
    AUIPC   (Type.U, 0b0010111),

    JAL     (Type.J, 0b1101111),

    JALR    (Type.I, 0b000_00000_1100111),

    BEQ     (Type.B, 0b000_00000_1100011),
    BNE     (Type.B, 0b001_00000_1100011),
    BLT     (Type.B, 0b100_00000_1100011),
    BGE     (Type.B, 0b101_00000_1100011),
    BLTU    (Type.B, 0b110_00000_1100011),
    BGEU    (Type.B, 0b111_00000_1100011),

    LB      (Type.I, 0b000_00000_0000011),
    LH      (Type.I, 0b001_00000_0000011),
    LW      (Type.I, 0b010_00000_0000011),
    LBU     (Type.I, 0b100_00000_0000011),
    LHU     (Type.I, 0b101_00000_0000011),

    SB      (Type.S, 0b000_00000_0100011),
    SH      (Type.S, 0b001_00000_0100011),
    SW      (Type.S, 0b010_00000_0100011),

    ADDI    (Type.I, 0b000_00000_0010011),
    SLTI    (Type.I, 0b010_00000_0010011),
    SLTIU   (Type.I, 0b011_00000_0010011),
    XORI    (Type.I, 0b100_00000_0010011),
    ORI     (Type.I, 0b110_00000_0010011),
    ANDI    (Type.I, 0b111_00000_0010011),

    SLLI    (Type.R, 0b0000000_0000000000_001_00000_0010011),
    SRLI    (Type.R, 0b0000000_0000000000_101_00000_0010011),
    SRAI    (Type.R, 0b0100000_0000000000_101_00000_0010011),
    ADD     (Type.R, 0b0000000_0000000000_000_00000_0110011),
    SUB     (Type.R, 0b0100000_0000000000_000_00000_0110011),
    SLL     (Type.R, 0b0000000_0000000000_001_00000_0110011),
    SLT     (Type.R, 0b0000000_0000000000_010_00000_0110011),
    SLTU    (Type.R, 0b0000000_0000000000_011_00000_0110011),
    XOR     (Type.R, 0b0000000_0000000000_100_00000_0110011),
    SRL     (Type.R, 0b0000000_0000000000_101_00000_0110011),
    SRA     (Type.R, 0b0100000_0000000000_101_00000_0110011),
    OR      (Type.R, 0b0000000_0000000000_110_00000_0110011),
    AND     (Type.R, 0b0000000_0000000000_111_00000_0110011),

    ECALL   (Type.I, 0b000000000000_00000_000_00000_1110011),
    EBREAK  (Type.I, 0b000000000001_00000_000_00000_1110011),
    ;
    private final Type type;
    private final int code;

    RV32I(final Type type, final int code) {
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
