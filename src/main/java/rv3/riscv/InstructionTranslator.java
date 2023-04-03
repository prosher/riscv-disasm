package rv3.riscv;

import java.util.HashMap;
import java.util.Map;

public class InstructionTranslator {
    private static final int OPCODE_MASK    = 0b00000000000000000000000001111111;
    private static final int FUNCT3_MASK    = 0b00000000000000000111000000000000;
    private static final int FUNCT7_MASK    = 0b11111110000000000000000000000000;

    private static final Map<Integer, RiscvInstruction> CODE_TO_INSTRUCTION;

    static {
        CODE_TO_INSTRUCTION = new HashMap<>();
        for (RV32I v : RV32I.values()) {
            CODE_TO_INSTRUCTION.put(v.getCode(), v);
        }
        for (RV32M v : RV32M.values()) {
            CODE_TO_INSTRUCTION.put(v.getCode(), v);
        }
    }

    public static RiscvInstruction getInstruction(final int code) {
        int codeMask = 0;
        if (CODE_TO_INSTRUCTION.containsKey(code & (codeMask | OPCODE_MASK))) {
            codeMask |= OPCODE_MASK;
            if (CODE_TO_INSTRUCTION.containsKey(code & (codeMask | FUNCT3_MASK))) {
                codeMask |= FUNCT3_MASK;
                if (CODE_TO_INSTRUCTION.containsKey(code & (codeMask | FUNCT7_MASK))) {
                    return CODE_TO_INSTRUCTION.get(code & codeMask);
                }
                return CODE_TO_INSTRUCTION.get(code & codeMask);
            }
            return CODE_TO_INSTRUCTION.get(code & codeMask);
        }
        return null;
    }

    private static int subcode(final int code, final int l, final int r) {
        return code << (32 - r) >>> (32 - r + l);
    }

    private static int signedSubcode(final int code, final int l) {
        return code >> l;
    }


    public static int getRd(final int code) {
        return subcode(code, 7, 12);
    }

    public static int getRs1(final int code) {
        return subcode(code, 15, 20);
    }

    public static int getRs2(final int code) {
        return subcode(code, 20, 25);
    }

    public static int getItypeImm(final int code) {
        return signedSubcode(code, 20);
    }

    public static int getStypeImm(final int code) {
        return subcode(code, 7, 12) | signedSubcode(code, 25) << 5;
    }

    public static int getBtypeImm(final int code) {
        return subcode(code, 8, 12) << 1
                | subcode(code, 25, 31) << 5
                | subcode(code, 7, 8) << 11
                | signedSubcode(code, 31) << 12;
    }

    public static int getUtypeImm(final int code) {
        return signedSubcode(code, 12);
    }

    public static int getJtypeImm(final int code) {
        return subcode(code, 21, 31) << 1
                | subcode(code, 20, 21) << 11
                | subcode(code, 12, 20) << 12
                | signedSubcode(code, 31) << 20;
    }

    public static String registerToAbi(final int register) {
        if (register == 0) {
            return "zero";
        } else if (register == 1) {
            return "ra";
        } else if (register == 2) {
            return "sp";
        } else if (register == 3) {
            return "gp";
        } else if (register == 4) {
            return "tp";
        } else if (register >= 5 && register <= 7) {
            return "t" + (register - 5);
        } else if (register >= 8 && register <= 9) {
            return "s" + (register - 8);
        } else if (register >= 10 && register <= 17) {
            return "a" + (register - 10);
        } else if (register >= 18 && register <= 27) {
            return "s" + (register - 16);
        } else if (register >= 28 && register <= 31) {
            return "t" + (register - 25);
        } else {
            return "x" + register;
        }
    }
}
