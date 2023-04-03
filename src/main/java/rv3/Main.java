package rv3;

import java.io.*;
import rv3.riscv.RiscvInstruction;
import rv3.riscv.Type;

import static rv3.riscv.InstructionTranslator.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Can't find arguments");
            return;
        }
        if (!args[0].equals("rv3")) {
            System.out.println("Only rv3 is supported");
            return;
        }
        if (args.length != 3) {
            System.out.println("Usage: rv3 <input ELF file path> <disassembled output file path>");
            return;
        }
        final Elf32 elfFile;
        try {
            try (final InputStream input = new FileInputStream(args[1])) {
                elfFile = Elf32.from(input);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to locate the input file: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.out.println("Unable to read the file: " + e.getMessage());
            return;
        }
        final Elf32.Elf32Sym[] symtab = elfFile.getSymtab();
        final int[] instructions = elfFile.getInstructions();
        final int virtualAddress = elfFile.getVirtualAddress();
        final Labels labels = new Labels();
        labelSymtab(symtab, labels);
        labelInstructions(instructions, labels, virtualAddress);
        try {
            try (final Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(args[2])))) {
                dumpInstructions(writer, instructions, virtualAddress, labels);
                writer.write("\n");
                dumpSymtab(writer, elfFile.getSymtab());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to locate the output file: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Unable to write to the file: " + e.getMessage());
        }
    }

    private static void labelInstructions(int[] codes, Labels labels, int virtualAddress) {
        for (int i = 0; i < codes.length; i++) {
            int curAddr = virtualAddress + i * 4;
            RiscvInstruction instr = getInstruction(codes[i]);
            if (instr == null) {
                continue;
            }
            if (instr.getType() == Type.J) {
                labels.getLabel(curAddr + getJtypeImm(codes[i]));
            } else if (instr.getType() == Type.B) {
                labels.getLabel(curAddr + getBtypeImm(codes[i]));
            }

        }
    }

    private static void labelSymtab(final Elf32.Elf32Sym[] symtab, final Labels labels) {
        for (Elf32.Elf32Sym sym : symtab) {
            labels.insert(sym.getValue(), sym.getName());
        }
    }

    private static void dumpSymtab(final Writer writer, final Elf32.Elf32Sym[] symtab)
            throws IOException {
        writer.write(".symtab\n");
        writer.write("Symbol Value          	  Size Type     Bind      Vis     Index   Name\n");
        for (int i = 0; i < symtab.length; i++) {
            writer.write(String.format("[%4d] 0x%-15x %5d %-8s %-8s %-8s %6s %s\n",
                    i,
                    symtab[i].getValue(),
                    symtab[i].getSize(),
                    symtab[i].getType(),
                    symtab[i].getBind(),
                    symtab[i].getVisibility(),
                    symtab[i].getIndex(),
                    symtab[i].getName()
                    ));
        }
    }

    private static void dumpInstructions(final Writer writer, final int[] codes,
                                         final int virtualAddress, final Labels labels)
            throws IOException {
        writer.write(".text\n");
        for (int i = 0; i < codes.length; i++) {
            int code = codes[i];
            int curAddr = virtualAddress + i * 4;
            if (labels.containAddr(curAddr)) {
                writer.write(String.format("%08x   <%s>:\n",
                        curAddr,
                        labels.getLabel(curAddr)));
            }
            writer.write(String.format("   %05x:\t%08x\t\t", curAddr, code));
            final RiscvInstruction entry = getInstruction(code);
            if (entry == null) {
                writer.write("unknown_instruction\n");
                continue;
            }
            writer.write(String.format("%-7s\t", entry.getName().toLowerCase()));
            writer.write(switch (entry.getType()) {
                case R -> String.format("%s, %s, %s\n",
                        registerToAbi(getRd(code)),
                        registerToAbi(getRs1(code)),
                        registerToAbi(getRs2(code))
                );
                case I -> {
                    final String name = entry.getName();
                    if (name.equals("ECALL") || name.equals("EBREAK")) {
                        yield "\n";
                    }
                    if (name.charAt(0) == 'L' || name.charAt(0) == 'J') {
                        yield String.format("%s, %s(%s)\n",
                                registerToAbi(getRd(code)),
                                getItypeImm(code),
                                registerToAbi(getRs1(code)));
                    }
                    yield String.format("%s, %s, %s\n",
                            registerToAbi(getRd(code)),
                            registerToAbi(getRs1(code)),
                            getItypeImm(code)
                    );
                }
                case S -> String.format("%s, %s(%s)\n",
                        registerToAbi(getRs2(code)),
                        getStypeImm(code),
                        registerToAbi(getRs1(code))
                );
                case B -> {
                    final int addr = getBtypeImm(code);
                    yield String.format("%s, %s, %x <%s>\n",
                            registerToAbi(getRs1(code)),
                            registerToAbi(getRs2(code)),
                            curAddr + addr,
                            labels.getLabel(curAddr + addr)
                    );
                }
                case U -> String.format("%s, 0x%x\n",
                        registerToAbi(getRd(code)),
                        getUtypeImm(code)
                );
                case J -> {
                    final int addr = getJtypeImm(code);
                    yield String.format("%s, %x <%s>\n",
                            registerToAbi(getRd(code)),
                            curAddr + addr,
                            labels.getLabel(curAddr + addr)
                    );
                }
            });
        }
    }
}