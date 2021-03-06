/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 */
package libdivide.templates

import org.lwjgl.generator.*
import libdivide.*

val libdivide = "LibDivide".nativeClass(Module.LIBDIVIDE, prefixConstant = "LIBDIVIDE_", prefixMethod = "libdivide_", library = "lwjgl_libdivide") {
    nativeImport("libdivide.h")
    javaImport("static org.lwjgl.system.MathUtil.*")
    javaImport("static org.lwjgl.system.MemoryUtil.*")
    documentation =
        """
        Native bindings to ${url("https://libdivide.com/", "libdivide")}.

        libdivide allows you to replace expensive integer divides with comparatively cheap multiplication and bitshifts. Compilers usually do this, but
        only when the divisor is known at compile time. libdivide allows you to take advantage of it at runtime. The result is that integer division can become
        faster - a lot faster.

        <b>LWJGL</b>: This is a hybrid implementation. Divider recovery methods use standard JNI bindings. All {@code *_gen}, {@code *_do} &amp;
        {@code *_get_algorithm} functions have been ported to pure Java. This eliminates the JNI overhead and enables the JVM to inline and further optimize
        these methods.
        """

    EnumConstant(
        "",
        
        "32_SHIFT_MASK".enum(0x1F),
        "64_SHIFT_MASK".enum(0x3F),
        "ADD_MARKER".enum(0x40),
        "U32_SHIFT_PATH".enum(0x80),
        "U64_SHIFT_PATH".enum(0x80),
        "S32_SHIFT_PATH".enum(0x20),
        "NEGATIVE_DIVISOR".enum(0x80)
    )

    NativeName("libdivide_s32_gen")..internal..libdivide_s32_t("s32_gen_ref", "", int32_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_s32_t")
    public static LibDivideS32 libdivide_s32_gen(@NativeType("int32_t") int denom, @NativeType("struct libdivide_s32_t") LibDivideS32 __result) {
        if (denom == 0) {
            throw new IllegalArgumentException("divider must be != 0");
        }

        int magic, more;

        int absD          = denom < 0 ? -denom : denom;
        int floor_log_2_d = 31 - Integer.numberOfLeadingZeros(absD);

        if ((absD & (absD - 1)) == 0) {
            magic = 0;
            more = floor_log_2_d | (denom < 0 ? LIBDIVIDE_NEGATIVE_DIVISOR : 0) | LIBDIVIDE_S32_SHIFT_PATH;
        } else {
            long l = 1L << (31 + floor_log_2_d);

            magic = (int)(l / absD);
            int rem = (int)(l % absD);

            if (Integer.compareUnsigned(absD - rem, 1 << floor_log_2_d) < 0) {
                more = floor_log_2_d - 1;
            } else {
                more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
                magic <<= 1;
                if (Integer.compareUnsigned(rem << 1, rem) < 0 || Integer.compareUnsigned(absD, rem << 1) < 0) {
                    magic++;
                }
            }

            magic++;
            if (denom < 0) {
                more |= LIBDIVIDE_NEGATIVE_DIVISOR;
                magic = -magic;
            }
        }

        __result.magic(magic);
        __result.more((byte)more);
        return __result;
    }""")

    NativeName("libdivide_u32_gen")..internal..libdivide_u32_t("u32_gen_ref", "", uint32_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_u32_t")
    public static LibDivideU32 libdivide_u32_gen(@NativeType("uint32_t") int denom, @NativeType("struct libdivide_u32_t") LibDivideU32 __result) {
        if (denom == 0) {
            throw new IllegalArgumentException("divider must be != 0");
        }

        int magic, more;

        int floor_log_2_d = 31 - Integer.numberOfLeadingZeros(denom);

        if ((denom & (denom - 1)) == 0) {
            magic = 0;
            more = floor_log_2_d | LIBDIVIDE_U32_SHIFT_PATH;
        } else {
            long l = 1L << (32 + floor_log_2_d);

            magic = (int)mathDivideUnsigned(l, Integer.toUnsignedLong(denom));
            int rem = (int)(l - magic * Integer.toUnsignedLong(denom));

            if (Integer.compareUnsigned(denom - rem, 1 << floor_log_2_d) < 0) {
                more = floor_log_2_d;
            } else {
                more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
                magic <<= 1;
                if (Integer.compareUnsigned(denom, rem << 1) < 0 || Integer.compareUnsigned(rem << 1, rem) < 0) {
                    magic++;
                }
            }

            magic++;
        }

        __result.magic(magic);
        __result.more((byte)more);
        return __result;
    }""")

    NativeName("libdivide_s64_gen")..internal..libdivide_s64_t("s64_gen_ref", "", int64_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_s64_t")
    public static LibDivideS64 libdivide_s64_gen(@NativeType("int64_t") long denom, @NativeType("struct libdivide_s64_t") LibDivideS64 __result) {
        if (denom == 0L) {
            throw new IllegalArgumentException("divider must be != 0");
        }

        long magic;
        int  more;

        long absD          = denom < 0L ? -denom : denom;
        int  floor_log_2_d = 63 - Long.numberOfLeadingZeros(absD);

        if ((absD & (absD - 1)) == 0) {
            magic = 0L;
            more = floor_log_2_d | (denom < 0L ? LIBDIVIDE_NEGATIVE_DIVISOR : 0);
        } else {
            magic = libdivide_128_div_64_to_64(1L << (floor_log_2_d - 1), 0L, absD, __result.address());
            long rem = __result.magic();

            if (Long.compareUnsigned(absD - rem, 1L << floor_log_2_d) < 0) {
                more = floor_log_2_d - 1;
            } else {
                more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
                magic <<= 1;
                if (Long.compareUnsigned(rem << 1, rem) < 0 || Long.compareUnsigned(absD, rem << 1) < 0) {
                    magic++;
                }
            }

            magic++;
            if (denom < 0) {
                more |= LIBDIVIDE_NEGATIVE_DIVISOR;
                magic = -magic;
            }
        }

        __result.magic(magic);
        __result.more((byte)more);
        return __result;
    }""")

    NativeName("libdivide_u64_gen")..internal..libdivide_u64_t("u64_gen_ref", "", uint64_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_u64_t")
    public static LibDivideU64 libdivide_u64_gen(@NativeType("uint64_t") long denom, @NativeType("struct libdivide_u64_t") LibDivideU64 __result) {
        if (denom == 0L) {
            throw new IllegalArgumentException("divider must be != 0");
        }

        long magic;
        int  more;

        int floor_log_2_d = 63 - Long.numberOfLeadingZeros(denom);

        if ((denom & (denom - 1)) == 0) {
            magic = 0;
            more = floor_log_2_d | LIBDIVIDE_U64_SHIFT_PATH;
        } else {
            magic = libdivide_128_div_64_to_64(1L << floor_log_2_d, 0L, denom, __result.address());
            long rem = __result.magic();

            if (Long.compareUnsigned(denom - rem, 1L << floor_log_2_d) < 0) {
                more = floor_log_2_d;
            } else {
                more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
                magic <<= 1;
                if (Long.compareUnsigned(denom, rem << 1) < 0 || Long.compareUnsigned(rem << 1, rem) < 0) {
                    magic++;
                }
            }

            magic++;
        }

        __result.magic(magic);
        __result.more((byte)more);
        return __result;
    }""")
    
    NativeName("libdivide_s32_branchfree_gen")..internal..libdivide_s32_branchfree_t("s32_branchfree_gen_ref", "", int32_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_s32_branchfree_t")
    public static LibDivideS32BranchFree libdivide_s32_branchfree_gen(@NativeType("int32_t") int denom, @NativeType("struct libdivide_s32_branchfree_t") LibDivideS32BranchFree __result) {
        if (denom == 0) {
            throw new IllegalArgumentException("divider must be != 0");
        }
        if (denom == 1) {
            throw new IllegalArgumentException("divider must be != 1");
        }
        if (denom == -1) {
            throw new IllegalArgumentException("divider must be != -1");
        }

        int magic, more;

        int absD          = denom < 0 ? -denom : denom;
        int floor_log_2_d = 31 - Integer.numberOfLeadingZeros(absD);

        if ((absD & (absD - 1)) == 0) {
            magic = 0;
            more = floor_log_2_d | (denom < 0 ? LIBDIVIDE_NEGATIVE_DIVISOR : 0) | LIBDIVIDE_S32_SHIFT_PATH;
        } else {
            long l = 1L << (31 + floor_log_2_d);

            magic = (int)(l / absD);
            int rem = (int)(l % absD);

            magic = (magic << 1) + 1;
            if (Integer.compareUnsigned(rem << 1, rem) < 0 || Integer.compareUnsigned(absD, rem << 1) < 0) {
                magic++;
            }

            more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
            if (denom < 0) {
                more |= LIBDIVIDE_NEGATIVE_DIVISOR;
            }
        }

        __result.magic(magic);
        __result.more((byte)more);
        return __result;
    }""")

    NativeName("libdivide_u32_branchfree_gen")..internal..libdivide_u32_branchfree_t("u32_branchfree_gen_ref", "", uint32_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_u32_branchfree_t")
    public static LibDivideU32BranchFree libdivide_u32_branchfree_gen(@NativeType("uint32_t") int denom, @NativeType("struct libdivide_u32_branchfree_t") LibDivideU32BranchFree __result) {
        if (denom == 0) {
            throw new IllegalArgumentException("divider must be != 0");
        }
        if (denom == 1) {
            throw new IllegalArgumentException("divider must be != 1");
        }
        if (denom == -1) {
            throw new IllegalArgumentException("divider must be != -1");
        }

        int magic, more;

        int floor_log_2_d = 31 - Integer.numberOfLeadingZeros(denom);

        if ((denom & (denom - 1)) == 0) {
            magic = 0;
            more = (floor_log_2_d - 1) | LIBDIVIDE_U32_SHIFT_PATH;
        } else {
            long l = 1L << (32 + floor_log_2_d);

            magic = (int)mathDivideUnsigned(l, Integer.toUnsignedLong(denom));
            int rem = (int)(l - magic * Integer.toUnsignedLong(denom));

            magic = (magic << 1) + 1;
            if (Integer.compareUnsigned(denom, rem << 1) < 0 || Integer.compareUnsigned(rem << 1, rem) < 0) {
                magic++;
            }

            more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
        }

        __result.magic(magic);
        __result.more((byte)(more & LIBDIVIDE_32_SHIFT_MASK));
        return __result;
    }""")

    NativeName("libdivide_s64_branchfree_gen")..internal..libdivide_s64_branchfree_t("s64_branchfree_gen_ref", "", int64_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_s64_branchfree_t")
    public static LibDivideS64BranchFree libdivide_s64_branchfree_gen(@NativeType("int64_t") long denom, @NativeType("struct libdivide_s64_branchfree_t") LibDivideS64BranchFree __result) {
        if (denom == 0L) {
            throw new IllegalArgumentException("divider must be != 0");
        }
        if (denom == 1L) {
            throw new IllegalArgumentException("divider must be != 1");
        }
        if (denom == -1L) {
            throw new IllegalArgumentException("divider must be != -1");
        }

        long magic;
        int  more;

        long absD          = denom < 0L ? -denom : denom;
        int  floor_log_2_d = 63 - Long.numberOfLeadingZeros(absD);

        if ((absD & (absD - 1)) == 0) {
            magic = 0L;
            more = floor_log_2_d | (denom < 0L ? LIBDIVIDE_NEGATIVE_DIVISOR : 0);
        } else {
            magic = (libdivide_128_div_64_to_64(1L << (floor_log_2_d - 1), 0L, absD, __result.address()) << 1) + 1;
            long rem = __result.magic();
            if (Long.compareUnsigned(rem << 1, rem) < 0 || Long.compareUnsigned(absD, rem << 1) < 0) {
                magic++;
            }

            more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
            if (denom < 0) {
                more |= LIBDIVIDE_NEGATIVE_DIVISOR;
            }
        }

        __result.magic(magic);
        __result.more((byte)more);
        return __result;
    }
""")

    NativeName("libdivide_u64_branchfree_gen")..internal..libdivide_u64_branchfree_t("u64_branchfree_gen_ref", "", uint64_t.IN("denom", ""))
    customMethod("""
    @NativeType("struct libdivide_u64_branchfree_t")
    public static LibDivideU64BranchFree libdivide_u64_branchfree_gen(@NativeType("uint64_t") long denom, @NativeType("struct libdivide_u64_branchfree_t") LibDivideU64BranchFree __result) {
        if (denom == 0L) {
            throw new IllegalArgumentException("divider must be != 0");
        }

        long magic;
        int  more;

        int floor_log_2_d = 63 - Long.numberOfLeadingZeros(denom);

        if ((denom & (denom - 1)) == 0) {
            magic = 0;
            more = (floor_log_2_d - 1) | LIBDIVIDE_U64_SHIFT_PATH;
        } else {
            magic = (libdivide_128_div_64_to_64(1L << floor_log_2_d, 0L, denom, __result.address()) << 1) + 1;
            long rem = __result.magic();
            if (Long.compareUnsigned(denom, rem << 1) < 0 || Long.compareUnsigned(rem << 1, rem) < 0) {
                magic++;
            }

            more = floor_log_2_d | LIBDIVIDE_ADD_MARKER;
        }

        __result.magic(magic);
        __result.more((byte)(more & LIBDIVIDE_64_SHIFT_MASK));
        return __result;
    }""")

    NativeName("libdivide_s32_do")..internal..int32_t("s32_do_ref", "", int32_t.IN("numer", ""), libdivide_s32_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_s32_do(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        byte more = denom.more();
        if ((more & LIBDIVIDE_S32_SHIFT_PATH) != 0) {
            int sign    = more >> 7;
            int shifter = (byte)(more & LIBDIVIDE_32_SHIFT_MASK);
            int q       = (numer + ((numer >> 31) & ((1 << shifter) - 1)));
            q >>= shifter;
            q = (q ^ sign) - sign;
            return q;
        } else {
            int uq = libdivide__mullhi_s32(denom.magic(), numer);
            if ((more & LIBDIVIDE_ADD_MARKER) != 0) {
                // must be arithmetic shift and then sign extend
                int sign = more >> 7;
                // q += (more < 0 ? -numer : numer), casts to avoid UB
                uq += (numer ^ sign) - sign;
            }
            int q = uq;
            q >>= more & LIBDIVIDE_32_SHIFT_MASK;
            q += (q < 0 ? 1 : 0);
            return q;
        }
    }""")

    NativeName("libdivide_u32_do")..internal..uint32_t("u32_do_ref", "", uint32_t.IN("numer", ""), libdivide_u32_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_u32_do(@NativeType("uint32_t") int numer, @NativeType("struct libdivide_u32_t const *") LibDivideU32 denom) {
        byte more = denom.more();
        if ((more & LIBDIVIDE_U32_SHIFT_PATH) != 0) {
            return numer >>> (more & LIBDIVIDE_32_SHIFT_MASK);
        } else {
            int q = libdivide__mullhi_u32(denom.magic(), numer);
            if ((more & LIBDIVIDE_ADD_MARKER) != 0) {
                int t = ((numer - q) >>> 1) + q;
                return t >>> (more & LIBDIVIDE_32_SHIFT_MASK);
            } else {
                // all upper bits are 0 - don't need to mask them off
                return q >>> more;
            }
        }
    }""")

    NativeName("libdivide_s64_do")..internal..int64_t("s64_do_ref", "", int64_t.IN("numer", ""), libdivide_s64_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static long libdivide_s64_do(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        byte more  = denom.more();
        long magic = denom.magic();
        if (magic == 0L) { //shift path
            int  shifter = more & LIBDIVIDE_64_SHIFT_MASK;
            long q       = numer + ((numer >> 63) & ((1L << shifter) - 1L));
            q >>= shifter;
            // must be arithmetic shift and then sign-extend
            long shiftMask = more >> 7;
            q = (q ^ shiftMask) - shiftMask;
            return q;
        } else {
            long uq = mathMultiplyHighS64(magic, numer);
            if ((more & LIBDIVIDE_ADD_MARKER) != 0) {
                // must be arithmetic shift and then sign extend
                long sign = more >> 7;
                uq += (numer ^ sign) - sign;
            }
            long q = uq;
            q >>= more & LIBDIVIDE_64_SHIFT_MASK;
            q += (q < 0 ? 1 : 0);
            return q;
        }
    }""")

    NativeName("libdivide_u64_do")..internal..uint64_t("u64_do_ref", "", uint64_t.IN("numer", ""), libdivide_u64_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static long libdivide_u64_do(@NativeType("uint64_t") long numer, @NativeType("struct libdivide_u64_t const *") LibDivideU64 denom) {
        byte more = denom.more();
        if ((more & LIBDIVIDE_U64_SHIFT_PATH) != 0) {
            return numer >>> (more & LIBDIVIDE_64_SHIFT_MASK);
        } else {
            long q = mathMultiplyHighU64(denom.magic(), numer);
            if ((more & LIBDIVIDE_ADD_MARKER) != 0) {
                long t = ((numer - q) >>> 1) + q;
                return t >>> (more & LIBDIVIDE_64_SHIFT_MASK);
            } else {
                // all upper bits are 0 - don't need to mask them off
                return q >>> more;
            }
        }
    }""")

    NativeName("libdivide_s32_branchfree_do")..internal..int32_t("s32_branchfree_do_ref", "", int32_t.IN("numer", ""), libdivide_s32_branchfree_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_s32_branchfree_do(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_branchfree_t const *") LibDivideS32BranchFree denom) {
        byte more  = denom.more();
        int  shift = more & LIBDIVIDE_32_SHIFT_MASK;
        int  sign  = more >> 7;
        int  magic = denom.magic();
        int  q     = libdivide__mullhi_s32(magic, numer);
        q += numer;

        int is_power_of_2 = (more & LIBDIVIDE_S32_SHIFT_PATH) >>> 5;
        int q_sign        = q >> 31;
        q += q_sign & ((1 << shift) - is_power_of_2);

        q >>= shift;

        q = (q ^ sign) - sign;

        return q;
    }""")

    NativeName("libdivide_u32_branchfree_do")..internal..uint32_t("u32_branchfree_do_ref", "", uint32_t.IN("numer", ""), libdivide_u32_branchfree_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_u32_branchfree_do(@NativeType("uint32_t") int numer, @NativeType("struct libdivide_u32_branchfree_t const *") LibDivideU32BranchFree denom) {
        int q = libdivide__mullhi_u32(denom.magic(), numer);
        int t = ((numer - q) >>> 1) + q;
        return t >>> denom.more();
    }""")

    NativeName("libdivide_s64_branchfree_do")..internal..int64_t("s64_branchfree_do_ref", "", int64_t.IN("numer", ""), libdivide_s64_branchfree_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static long libdivide_s64_branchfree_do(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_branchfree_t const *") LibDivideS64BranchFree denom) {
        byte more  = denom.more();
        int  shift = more & LIBDIVIDE_64_SHIFT_MASK;
        long sign  = more >> 7;
        long magic = denom.magic();
        long q     = mathMultiplyHighS64(magic, numer);
        q += numer;

        int  is_power_of_2 = magic == 0 ? 1 : 0;
        long q_sign        = q >> 63;
        q += q_sign & ((1L << shift) - is_power_of_2);

        q >>= shift;

        q = (q ^ sign) - sign;
        return q;
    }""")

    NativeName("libdivide_u64_branchfree_do")..internal..uint64_t("u64_branchfree_do_ref", "", uint64_t.IN("numer", ""), libdivide_u64_branchfree_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static long libdivide_u64_branchfree_do(@NativeType("uint64_t") long numer, @NativeType("struct libdivide_u64_branchfree_t const *") LibDivideU64BranchFree denom) {
        long q = mathMultiplyHighU64(denom.magic(), numer);
        long t = ((numer - q) >>> 1) + q;
        return t >>> denom.more();
    }""")

    int32_t("s32_recover", "", libdivide_s32_t.const.p.IN("denom", ""))
    uint32_t("u32_recover", "", libdivide_u32_t.const.p.IN("denom", ""))
    int64_t("s64_recover", "", libdivide_s64_t.const.p.IN("denom", ""))
    uint64_t("u64_recover", "", libdivide_u64_t.const.p.IN("denom", ""))

    int32_t("s32_branchfree_recover", "", libdivide_s32_branchfree_t.const.p.IN("denom", ""))
    uint32_t("u32_branchfree_recover", "", libdivide_u32_branchfree_t.const.p.IN("denom", ""))
    int64_t("s64_branchfree_recover", "", libdivide_s64_branchfree_t.const.p.IN("denom", ""))
    uint64_t("u64_branchfree_recover", "", libdivide_u64_branchfree_t.const.p.IN("denom", ""))

    //int("u32_get_algorithm", "", libdivide_u32_t.const.p.IN("denom", ""))
    //uint32_t("u32_do_alg0", "", uint32_t.IN("numer", ""), libdivide_u32_t.const.p.IN("denom", ""))
    //uint32_t("u32_do_alg1", "", uint32_t.IN("numer", ""), libdivide_u32_t.const.p.IN("denom", ""))
    //uint32_t("u32_do_alg2", "", uint32_t.IN("numer", ""), libdivide_u32_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_u32_get_algorithm(@NativeType("struct libdivide_u32_t const *") LibDivideU32 denom) {
        byte more = denom.more();
        if ((more & LIBDIVIDE_U32_SHIFT_PATH) != 0) {
            return 0;
        } else if ((more & LIBDIVIDE_ADD_MARKER) == 0) {
            return 1;
        } else {
            return 2;
        }
    }

    public static int libdivide_u32_do_alg0(@NativeType("uint32_t") int numer, @NativeType("struct libdivide_u32_t const *") LibDivideU32 denom) {
        return numer >>> (denom.more() & LIBDIVIDE_32_SHIFT_MASK);
    }

    public static int libdivide_u32_do_alg1(@NativeType("uint32_t") int numer, @NativeType("struct libdivide_u32_t const *") LibDivideU32 denom) {
        int q = libdivide__mullhi_u32(denom.magic(), numer);
        return q >>> denom.more();
    }

    public static int libdivide_u32_do_alg2(@NativeType("uint32_t") int numer, @NativeType("struct libdivide_u32_t const *") LibDivideU32 denom) {
        // denom->add != 0
        int q = libdivide__mullhi_u32(denom.magic(), numer);
        int t = ((numer - q) >>> 1) + q;
        // Note that this mask is typically free. Only the low bits are meaningful
        // to a shift, so compilers can optimize out this AND.
        return t >>> (denom.more() & LIBDIVIDE_32_SHIFT_MASK);
    }""")

    //int("u64_get_algorithm", "", libdivide_u64_t.const.p.IN("denom", ""))
    //uint64_t("u64_do_alg0", "", uint64_t.IN("numer", ""), libdivide_u64_t.const.p.IN("denom", ""))
    //uint64_t("u64_do_alg1", "", uint64_t.IN("numer", ""), libdivide_u64_t.const.p.IN("denom", ""))
    //uint64_t("u64_do_alg2", "", uint64_t.IN("numer", ""), libdivide_u64_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_u64_get_algorithm(@NativeType("struct libdivide_u64_t const *") LibDivideU64 denom) {
        byte more = denom.more();
        if ((more & LIBDIVIDE_U64_SHIFT_PATH) != 0) {
            return 0;
        } else if ((more & LIBDIVIDE_ADD_MARKER) == 0) {
            return 1;
        } else {
            return 2;
        }
    }

    public static long libdivide_u64_do_alg0(@NativeType("uint64_t") long numer, @NativeType("struct libdivide_u64_t const *") LibDivideU64 denom) {
        return numer >>> (denom.more() & LIBDIVIDE_64_SHIFT_MASK);
    }

    public static long libdivide_u64_do_alg1(@NativeType("uint64_t") long numer, @NativeType("struct libdivide_u64_t const *") LibDivideU64 denom) {
        long q = mathMultiplyHighU64(denom.magic(), numer);
        return q >>> denom.more();
    }

    public static long libdivide_u64_do_alg2(@NativeType("uint64_t") long numer, @NativeType("struct libdivide_u64_t const *") LibDivideU64 denom) {
        long q = mathMultiplyHighU64(denom.magic(), numer);
        long t = ((numer - q) >>> 1) + q;
        return t >>> (denom.more() & LIBDIVIDE_64_SHIFT_MASK);
    }""")

    //int("s32_get_algorithm", "", libdivide_s32_t.const.p.IN("denom", ""))
    //int32_t("s32_do_alg0", "", int32_t.IN("numer", ""), libdivide_s32_t.const.p.IN("denom", ""))
    //int32_t("s32_do_alg1", "", int32_t.IN("numer", ""), libdivide_s32_t.const.p.IN("denom", ""))
    //int32_t("s32_do_alg2", "", int32_t.IN("numer", ""), libdivide_s32_t.const.p.IN("denom", ""))
    //int32_t("s32_do_alg3", "", int32_t.IN("numer", ""), libdivide_s32_t.const.p.IN("denom", ""))
    //int32_t("s32_do_alg4", "", int32_t.IN("numer", ""), libdivide_s32_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_s32_get_algorithm(@NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        byte    more            = denom.more();
        boolean positiveDivisor = (more & LIBDIVIDE_NEGATIVE_DIVISOR) == 0;
        if ((more & LIBDIVIDE_S32_SHIFT_PATH) != 0) {
            return (positiveDivisor ? 0 : 1);
        } else if ((more & LIBDIVIDE_ADD_MARKER) != 0) {
            return (positiveDivisor ? 2 : 3);
        } else {
            return 4;
        }
    }

    public static int libdivide_s32_do_alg0(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        int shifter = denom.more() & LIBDIVIDE_32_SHIFT_MASK;
        return (numer + ((numer >> 31) & ((1 << shifter) - 1))) >> shifter;
    }

    public static int libdivide_s32_do_alg1(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        int shifter = denom.more() & LIBDIVIDE_32_SHIFT_MASK;
        return -((numer + ((numer >> 31) & ((1 << shifter) - 1))) >> shifter);
    }

    public static int libdivide_s32_do_alg2(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        int q = libdivide__mullhi_s32(denom.magic(), numer);
        q += numer;
        q >>= denom.more() & LIBDIVIDE_32_SHIFT_MASK;
        q += (q < 0 ? 1 : 0);
        return q;
    }

    public static int libdivide_s32_do_alg3(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        int q = libdivide__mullhi_s32(denom.magic(), numer);
        q -= numer;
        q >>= denom.more() & LIBDIVIDE_32_SHIFT_MASK;
        q += (q < 0 ? 1 : 0);
        return q;
    }

    public static int libdivide_s32_do_alg4(@NativeType("int32_t") int numer, @NativeType("struct libdivide_s32_t const *") LibDivideS32 denom) {
        int q = libdivide__mullhi_s32(denom.magic(), numer);
        q >>= denom.more() & LIBDIVIDE_32_SHIFT_MASK;
        q += (q < 0 ? 1 : 0);
        return q;
    }""")

    //int("s64_get_algorithm", "", libdivide_s64_t.const.p.IN("denom", ""))
    //int64_t("s64_do_alg0", "", int64_t.IN("numer", ""), libdivide_s64_t.const.p.IN("denom", ""))
    //int64_t("s64_do_alg1", "", int64_t.IN("numer", ""), libdivide_s64_t.const.p.IN("denom", ""))
    //int64_t("s64_do_alg2", "", int64_t.IN("numer", ""), libdivide_s64_t.const.p.IN("denom", ""))
    //int64_t("s64_do_alg3", "", int64_t.IN("numer", ""), libdivide_s64_t.const.p.IN("denom", ""))
    //int64_t("s64_do_alg4", "", int64_t.IN("numer", ""), libdivide_s64_t.const.p.IN("denom", ""))
    customMethod(
        """
    public static int libdivide_s64_get_algorithm(@NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        byte    more            = denom.more();
        boolean positiveDivisor = (more & LIBDIVIDE_NEGATIVE_DIVISOR) == 0;
        if (denom.magic() == 0) {
            return (positiveDivisor ? 0 : 1); // shift path
        } else if ((more & LIBDIVIDE_ADD_MARKER) != 0) {
            return (positiveDivisor ? 2 : 3);
        } else {
            return 4;
        }
    }

    public static long libdivide_s64_do_alg0(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        int shifter = denom.more() & LIBDIVIDE_64_SHIFT_MASK;
        return (numer + ((numer >> 63) & ((1L << shifter) - 1L))) >> shifter;
    }

    public static long libdivide_s64_do_alg1(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        int shifter = denom.more() & LIBDIVIDE_64_SHIFT_MASK;
        return -((numer + ((numer >> 63) & ((1L << shifter) - 1L))) >> shifter);
    }

    public static long libdivide_s64_do_alg2(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        long q = mathMultiplyHighS64(denom.magic(), numer);
        q += numer;
        q >>= denom.more() & LIBDIVIDE_64_SHIFT_MASK;
        q += (q < 0 ? 1 : 0);
        return q;
    }

    public static long libdivide_s64_do_alg3(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        long q = mathMultiplyHighS64(denom.magic(), numer);
        q -= numer;
        q >>= denom.more() & LIBDIVIDE_64_SHIFT_MASK;
        q += (q < 0 ? 1 : 0);
        return q;
    }

    public static long libdivide_s64_do_alg4(@NativeType("int64_t") long numer, @NativeType("struct libdivide_s64_t const *") LibDivideS64 denom) {
        long q = mathMultiplyHighS64(denom.magic(), numer);
        q >>= denom.more() & LIBDIVIDE_64_SHIFT_MASK;
        q += (q < 0 ? 1 : 0);
        return q;
    }""")

    // Helper methods

    customMethod("""
     private static long libdivide_128_div_64_to_64(long u1, long u0, long v, long remainder) {
        long b = (1L << 32);

        long un64, un10;

        int s = Long.numberOfLeadingZeros(v);
        if (s > 0) {
            v <<= s;
            un64 = (u1 << s) | ((u0 >>> (64 - s)) & (-s >> 31));
            un10 = u0 << s;
        } else {
            un64 = u1 | u0;
            un10 = u0;
        }

        long vn1 = v >>> 32;
        long vn0 = v & 0xFFFF_FFFFL;

        long un1 = un10 >>> 32;
        long un0 = un10 & 0xFFFF_FFFFL;

        long q1   = mathDivideUnsigned(un64, vn1);
        long rhat = un64 - q1 * vn1;

        while (Long.compareUnsigned(b, q1) < 0 || Long.compareUnsigned(b * rhat + un1, q1 * vn0) <= 0) {
            q1--;
            rhat += vn1;
            if (rhat >= b) {
                break;
            }
        }

        long un21 = un64 * b + un1 - q1 * v;

        long q0 = mathDivideUnsigned(un21, vn1);
        rhat = un21 - q0 * vn1;

        while (Long.compareUnsigned(b, q0) < 0 || Long.compareUnsigned(b * rhat + un0, q0 * vn0) <= 0) {
            q0--;
            rhat += vn1;
            if (rhat >= b) {
                break;
            }
        }

        memPutLong(remainder, (un21 * b + un0 - q0 * v) >>> s);
        return q1 * b + q0;
    }

    private static int libdivide__mullhi_s32(int x, int y) {
        return (int)(((long)x * (long)y) >> 32);
    }

    private static int libdivide__mullhi_u32(int x, int y) {
        return (int)(((x & 0xFFFF_FFFFL) * (y & 0xFFFF_FFFFL)) >>> 32);
    }""")

}