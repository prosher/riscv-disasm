package rv3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Elf32 {
    // elf32 file header
//    private static final int EI_NIDENT = 16;
//    private String e_ident;
//    private short e_type;
//    private short e_machine;
//    private int e_version;
    private int e_entry;
//    private int e_phoff;
    private int e_shoff;
//    private int e_flags;
//    private short e_ehsize;
//    private short e_phentsize;
//    private short e_phnum;
    private short e_shentsize;
    private short e_shnum;
    private short e_shstrndx;


    public class ELf32Shdr {
        private int      sh_name;
//        private int      sh_type;
//        private int      sh_flags;
//        private int      sh_addr;
        private int      sh_offset;
        private int      sh_size;
//        private int      sh_link;
//        private int      sh_info;
//        private int      sh_addralign;
        private int      sh_entsize;

        public String getName() {
            return getString(stringTable, sh_name);
        }
    }
    private ELf32Shdr[] sections;


    public class Elf32Sym {
        private int    st_name;
        private int    st_value;
        private int    st_size;
        private char   st_info;
        private char   st_other;
        private short  st_shndx;

        private static final Map<Integer, String> ST_TYPE = Map.ofEntries(
                Map.entry(0, "NOTYPE"),
                Map.entry(1, "OBJECT"),
                Map.entry(2, "FUNC"),
                Map.entry(3, "SECTION"),
                Map.entry(4, "FILE"),
                Map.entry(5, "COMMON"),
                Map.entry(10, "LOOS"),
                Map.entry(12, "HIOS"),
                Map.entry(13, "LOPROC"),
                Map.entry(15, "HIPROC")
        );

        private static final Map<Integer, String> ST_BIND = Map.of(
                0, "LOCAL",
                1, "GLOBAL",
                2, "WEAK",
                10, "LOOS",
                12, "HIOS",
                13, "LOPROC",
                15, "HIPROC"
        );

        private static final Map<Integer, String> ST_VISIBILITY = Map.of(
                0, "DEFAULT",
                1, "INTERNAL",
                2, "HIDDEN",
                3, "PROTECTED"
        );
        public int getValue() {
            return st_value;
        }
        public int getSize() {
            return st_size;
        }
        public String getType() {
            return ST_TYPE.get(st_info & 0xf);
        }
        public String getBind() {
            return ST_BIND.get(st_info >> 4);
        }
        public String getVisibility() {
            return ST_VISIBILITY.get(st_other & 0x3);
        }
        public String getName() {
            return getString(symStringTable, st_name);
        }
        public String getIndex() {
            if (st_shndx == 0) {
                return "UNDEF";
            }
            if (st_shndx == (short)0xfff1) {
                return "ABS";
            }
            if (st_shndx == (short)0xfff2) {
                return "COMMON";
            }
            return Short.toString(st_shndx);
        }
    }
    private Elf32Sym[] symtable;

    private String stringTable;
    private String symStringTable;

    private int[] instructions32;

    private String getString(final String table, final int offset) {
        int i = offset;
        while (table.charAt(i) != 0) {
            i++;
        }
        return table.substring(offset, i);
    }
    public int[] getInstructions() {
        return instructions32;
    }

    public Elf32Sym[] getSymtab() {
        return symtable;
    }

    public int getVirtualAddress() {
        return e_entry;
    }

    public static Elf32 from(final InputStream input) throws IOException {
        return new ElfParser().parse(input);
    }

    private static class ElfParser {
        private byte[] data;
        public Elf32 parse(final InputStream input) throws IOException {
            data = input.readAllBytes();
            final Elf32 file = new Elf32();
            parseHeader(file);
            file.sections = new ELf32Shdr[file.e_shnum];
            for (int i = 0; i < file.sections.length; i++) {
                file.sections[i] = file.new ELf32Shdr();
                parseSectionHeader(file.sections[i], file.e_shoff + i * file.e_shentsize);
            }
            parseSections(file);
            return file;
        }

        private void parseSections(Elf32 file) {
            final ELf32Shdr shstr = file.sections[file.e_shstrndx];
            file.stringTable = new String(data, shstr.sh_offset, shstr.sh_size);
            for (ELf32Shdr shdr : file.sections) {
                switch (shdr.getName()) {
                    case ".text" -> parseInstructions(file, shdr);
                    case ".strtab" -> parseSymStrTable(file, shdr);
                    case ".symtab" -> parseSymtable(file, shdr);
                }
            }
        }

        private void parseSymStrTable(Elf32 file, ELf32Shdr shdr) {
            file.symStringTable = new String(data, shdr.sh_offset, shdr.sh_size);
        }

        private void parseSymtable(Elf32 file, ELf32Shdr shdr) {
            file.symtable = new Elf32Sym[shdr.sh_size / shdr.sh_entsize];
            for (int i = 0; i < file.symtable.length; i++) {
                file.symtable[i] = file.new Elf32Sym();
                parseSym(file.symtable[i], shdr.sh_offset + i * shdr.sh_entsize);
            }
        }

        private void parseSym(Elf32Sym sym, final int offset) {
            sym.st_name = getInt(offset);
            sym.st_value = getInt(offset + 4);
            sym.st_size = getInt(offset + 8);
            sym.st_info = (char)data[offset + 12];
            sym.st_other = (char)data[offset + 13];
            sym.st_shndx = getShort(offset + 14);
        }

        private void parseInstructions(Elf32 file, ELf32Shdr shdr) {
            file.instructions32 = new int[shdr.sh_size / 4];
            for (int i = 0; i < file.instructions32.length; i++) {
                file.instructions32[i] = getInt(shdr.sh_offset + 4 * i);
            }
        }

        private void parseSectionHeader(final ELf32Shdr section, final int offset) {
            section.sh_name = getInt(offset);
//            section.sh_type = getInt(offset + 4);
//            section.sh_flags = getInt(offset + 8);
//            section.sh_addr = getInt(offset + 12);
            section.sh_offset = getInt(offset + 16);
            section.sh_size = getInt(offset + 20);
//            section.sh_link = getInt(offset + 24);
//            section.sh_info = getInt(offset + 28);
//            section.sh_addralign = getInt(offset + 32);
            section.sh_entsize = getInt(offset + 36);
        }

        private void parseHeader(Elf32 elf) {
//            String e_ident = new String(Arrays.copyOfRange(data, 0, elf.EI_INDENT));
//            elf.e_type = getShort(16);
//            elf.e_machine = getShort(18);
//            elf.e_version = getInt(20);
            elf.e_entry = getInt(24);
//            elf.e_phoff = getInt(28);
            elf.e_shoff = getInt(32);
//            int      elf.e_flags = getInt(36);
//            short    elf.e_ehsize = getShort(40);
//            short    elf.e_phentsize = getShort(42);
//            short    elf.e_phnum = getShort(44);
            elf.e_shentsize = getShort(46);
            elf.e_shnum = getShort(48);
            elf.e_shstrndx = getShort(50);
        }

        private int getInt(final int offset) {
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result |= (data[i + offset] & 0xFF) << (i * 8);
            }
            return result;
        }

        private short getShort(final int offset) {
            short result = 0;
            for (int i = 0; i < 2; i++) {
                result |= (data[i + offset] & 0xFF) << (i * 8);
            }
            return result;
        }
    }

}
