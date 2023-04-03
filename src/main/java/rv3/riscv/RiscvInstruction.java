package rv3.riscv;

public interface RiscvInstruction {
    String getName();
    Type getType();
    int getCode();
}
