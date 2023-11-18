; ModuleID = 'llvm-link'
source_filename = "llvm-link"
target datalayout = "e-m:o-i64:64-i128:128-n32:64-S128"
target triple = "arm64-apple-macosx13.0.0"

@fib_matrix = dso_local global [2 x [2 x i32]] [[2 x i32] [i32 1, i32 1], [2 x i32] [i32 1, i32 0]]
@__res = dso_local global [2 x [2 x i32]] [[2 x i32] [i32 5, i32 5], [2 x i32] [i32 2, i32 3]]
@__useless = dso_local global [4 x i32] [i32 1, i32 1, i32 1, i32 0]
@.str = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.1 = private unnamed_addr constant [3 x i8] c"%c\00", align 1
@.str.2 = private unnamed_addr constant [4 x i8] c"%d:\00", align 1
@.str.3 = private unnamed_addr constant [4 x i8] c" %d\00", align 1
@.str.4 = private unnamed_addr constant [2 x i8] c"\0A\00", align 1
@.str.5 = private unnamed_addr constant [3 x i8] c"%s\00", align 1

define dso_local void @__vec_mul(i32* %res_1_4, [2 x i32]* %mat_1_5, i32* %vec_1_6) {
  br label %label_0

label_0:                                          ; preds = %0
  %-t0_1_32 = getelementptr i32, i32* %vec_1_6, i32 0
  %-t1_1_33 = load i32, i32* %-t0_1_32, align 4
  %-t2_1_34 = getelementptr [2 x i32], [2 x i32]* %mat_1_5, i32 0, i32 0
  %-t3_1_35 = load i32, i32* %-t2_1_34, align 4
  %-t4_1_36 = mul i32 %-t1_1_33, %-t3_1_35
  %-t5_1_37 = getelementptr i32, i32* %vec_1_6, i32 1
  %-t6_1_38 = load i32, i32* %-t5_1_37, align 4
  %-t7_1_39 = getelementptr [2 x i32], [2 x i32]* %mat_1_5, i32 0, i32 1
  %-t8_1_40 = load i32, i32* %-t7_1_39, align 4
  %-t9_1_41 = mul i32 %-t6_1_38, %-t8_1_40
  %-t10_1_42 = add i32 %-t4_1_36, %-t9_1_41
  %-t11_1_43 = getelementptr i32, i32* %res_1_4, i32 0
  store i32 %-t10_1_42, i32* %-t11_1_43, align 4
  %-t12_1_44 = getelementptr i32, i32* %vec_1_6, i32 0
  %-t13_1_45 = load i32, i32* %-t12_1_44, align 4
  %-t14_1_46 = getelementptr [2 x i32], [2 x i32]* %mat_1_5, i32 1, i32 0
  %-t15_1_47 = load i32, i32* %-t14_1_46, align 4
  %-t16_1_48 = mul i32 %-t13_1_45, %-t15_1_47
  %-t17_1_49 = getelementptr i32, i32* %vec_1_6, i32 1
  %-t18_1_50 = load i32, i32* %-t17_1_49, align 4
  %-t19_1_51 = getelementptr [2 x i32], [2 x i32]* %mat_1_5, i32 1, i32 1
  %-t20_1_52 = load i32, i32* %-t19_1_51, align 4
  %-t21_1_53 = mul i32 %-t18_1_50, %-t20_1_52
  %-t22_1_54 = add i32 %-t16_1_48, %-t21_1_53
  %-t23_1_55 = getelementptr i32, i32* %res_1_4, i32 1
  store i32 %-t22_1_54, i32* %-t23_1_55, align 4
  ret void

1:                                                ; No predecessors!
  ret void
}

define dso_local void @__mat_mul([2 x i32]* %res_1_8, [2 x i32]* %x_1_9, [2 x i32]* %y_1_10) {
  br label %label_1

label_1:                                          ; preds = %0
  %-t24_1_56 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 0, i32 0
  %-t25_1_57 = load i32, i32* %-t24_1_56, align 4
  %-t26_1_58 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 0, i32 0
  %-t27_1_59 = load i32, i32* %-t26_1_58, align 4
  %-t28_1_60 = mul i32 %-t25_1_57, %-t27_1_59
  %-t29_1_61 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 0, i32 1
  %-t30_1_62 = load i32, i32* %-t29_1_61, align 4
  %-t31_1_63 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 1, i32 0
  %-t32_1_64 = load i32, i32* %-t31_1_63, align 4
  %-t33_1_65 = mul i32 %-t30_1_62, %-t32_1_64
  %-t34_1_66 = add i32 %-t28_1_60, %-t33_1_65
  %-t35_1_67 = getelementptr [2 x i32], [2 x i32]* %res_1_8, i32 0, i32 0
  store i32 %-t34_1_66, i32* %-t35_1_67, align 4
  %-t36_1_68 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 0, i32 0
  %-t37_1_69 = load i32, i32* %-t36_1_68, align 4
  %-t38_1_70 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 0, i32 1
  %-t39_1_71 = load i32, i32* %-t38_1_70, align 4
  %-t40_1_72 = mul i32 %-t37_1_69, %-t39_1_71
  %-t41_1_73 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 0, i32 1
  %-t42_1_74 = load i32, i32* %-t41_1_73, align 4
  %-t43_1_75 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 1, i32 1
  %-t44_1_76 = load i32, i32* %-t43_1_75, align 4
  %-t45_1_77 = mul i32 %-t42_1_74, %-t44_1_76
  %-t46_1_78 = add i32 %-t40_1_72, %-t45_1_77
  %-t47_1_79 = getelementptr [2 x i32], [2 x i32]* %res_1_8, i32 0, i32 1
  store i32 %-t46_1_78, i32* %-t47_1_79, align 4
  %-t48_1_80 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 1, i32 0
  %-t49_1_81 = load i32, i32* %-t48_1_80, align 4
  %-t50_1_82 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 0, i32 0
  %-t51_1_83 = load i32, i32* %-t50_1_82, align 4
  %-t52_1_84 = mul i32 %-t49_1_81, %-t51_1_83
  %-t53_1_85 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 1, i32 1
  %-t54_1_86 = load i32, i32* %-t53_1_85, align 4
  %-t55_1_87 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 1, i32 0
  %-t56_1_88 = load i32, i32* %-t55_1_87, align 4
  %-t57_1_89 = mul i32 %-t54_1_86, %-t56_1_88
  %-t58_1_90 = add i32 %-t52_1_84, %-t57_1_89
  %-t59_1_91 = getelementptr [2 x i32], [2 x i32]* %res_1_8, i32 1, i32 0
  store i32 %-t58_1_90, i32* %-t59_1_91, align 4
  %-t60_1_92 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 1, i32 0
  %-t61_1_93 = load i32, i32* %-t60_1_92, align 4
  %-t62_1_94 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 0, i32 1
  %-t63_1_95 = load i32, i32* %-t62_1_94, align 4
  %-t64_1_96 = mul i32 %-t61_1_93, %-t63_1_95
  %-t65_1_97 = getelementptr [2 x i32], [2 x i32]* %x_1_9, i32 1, i32 1
  %-t66_1_98 = load i32, i32* %-t65_1_97, align 4
  %-t67_1_99 = getelementptr [2 x i32], [2 x i32]* %y_1_10, i32 1, i32 1
  %-t68_1_100 = load i32, i32* %-t67_1_99, align 4
  %-t69_1_101 = mul i32 %-t66_1_98, %-t68_1_100
  %-t70_1_102 = add i32 %-t64_1_96, %-t69_1_101
  %-t71_1_103 = getelementptr [2 x i32], [2 x i32]* %res_1_8, i32 1, i32 1
  store i32 %-t70_1_102, i32* %-t71_1_103, align 4
  ret void

1:                                                ; No predecessors!
  ret void
}

define dso_local i32 @__power(i32 %n_1_12, [2 x i32]* %cur_1_13, [2 x i32]* %res_1_14) {
  br label %label_2

label_2:                                          ; preds = %0
  %-t108_1_140 = alloca i32, align 4
  store i32 %n_1_12, i32* %-t108_1_140, align 4
  %-t72_1_104 = load i32, i32* %-t108_1_140, align 4
  %-t73_1_105 = icmp eq i32 %-t72_1_104, 1
  %-t74_1_106 = zext i1 %-t73_1_105 to i32
  %-t75_1_107 = icmp ne i32 %-t74_1_106, 0
  br i1 %-t75_1_107, label %label_if_4, label %label_else_5

1:                                                ; No predecessors!
  br label %label_else_5

label_else_5:                                     ; preds = %1, %label_2
  br label %label_6

label_6:                                          ; preds = %label_else_5
  %-t76_2_108 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 0
  %-t77_2_109 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 0
  %-t78_2_110 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* @fib_matrix, i32 0, i32 0
  call void @__mat_mul([2 x i32]* %-t76_2_108, [2 x i32]* %-t77_2_109, [2 x i32]* %-t78_2_110)
  %-t79_2_111 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 0, i32 0
  %-t80_2_112 = load i32, i32* %-t79_2_111, align 4
  %-t81_2_113 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 0, i32 0
  store i32 %-t80_2_112, i32* %-t81_2_113, align 4
  %-t82_2_114 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 0, i32 1
  %-t83_2_115 = load i32, i32* %-t82_2_114, align 4
  %-t84_2_116 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 0, i32 1
  store i32 %-t83_2_115, i32* %-t84_2_116, align 4
  %-t85_2_117 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 1, i32 0
  %-t86_2_118 = load i32, i32* %-t85_2_117, align 4
  %-t87_2_119 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 1, i32 0
  store i32 %-t86_2_118, i32* %-t87_2_119, align 4
  %-t88_2_120 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 1, i32 1
  %-t89_2_121 = load i32, i32* %-t88_2_120, align 4
  %-t90_2_122 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 1, i32 1
  store i32 %-t89_2_121, i32* %-t90_2_122, align 4
  %-t92_2_124 = load i32, i32* %-t108_1_140, align 4
  %-t93_2_125 = sub i32 %-t92_2_124, 1
  %-t94_2_126 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 0
  %-t95_2_127 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 0
  %-t91_2_123 = call i32 @__power(i32 %-t93_2_125, [2 x i32]* %-t94_2_126, [2 x i32]* %-t95_2_127)
  ret i32 %-t91_2_123

2:                                                ; No predecessors!
  br label %label_3

3:                                                ; No predecessors!
  br label %label_if_4

label_if_4:                                       ; preds = %3, %label_2
  br label %label_7

label_7:                                          ; preds = %label_if_4
  %-t96_2_128 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 0, i32 0
  %-t97_2_129 = load i32, i32* %-t96_2_128, align 4
  %-t98_2_130 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 0, i32 0
  store i32 %-t97_2_129, i32* %-t98_2_130, align 4
  %-t99_2_131 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 0, i32 1
  %-t100_2_132 = load i32, i32* %-t99_2_131, align 4
  %-t101_2_133 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 0, i32 1
  store i32 %-t100_2_132, i32* %-t101_2_133, align 4
  %-t102_2_134 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 1, i32 0
  %-t103_2_135 = load i32, i32* %-t102_2_134, align 4
  %-t104_2_136 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 1, i32 0
  store i32 %-t103_2_135, i32* %-t104_2_136, align 4
  %-t105_2_137 = getelementptr [2 x i32], [2 x i32]* %cur_1_13, i32 1, i32 1
  %-t106_2_138 = load i32, i32* %-t105_2_137, align 4
  %-t107_2_139 = getelementptr [2 x i32], [2 x i32]* %res_1_14, i32 1, i32 1
  store i32 %-t106_2_138, i32* %-t107_2_139, align 4
  ret i32 0

4:                                                ; No predecessors!
  br label %label_3

label_3:                                          ; preds = %4, %2
  ret i32 0

5:                                                ; No predecessors!
  ret i32 0
}

define dso_local i32 @power(i32 %n_1_16, [2 x i32]* %res_1_17) {
  br label %label_8

label_8:                                          ; preds = %0
  %-t122_1_154 = alloca i32, align 4
  store i32 %n_1_16, i32* %-t122_1_154, align 4
  %-t109_1_141 = load i32, i32* %-t122_1_154, align 4
  %-t110_1_142 = icmp sle i32 %-t109_1_141, 0
  %-t111_1_143 = zext i1 %-t110_1_142 to i32
  %-t112_1_144 = icmp ne i32 %-t111_1_143, 0
  br i1 %-t112_1_144, label %label_if_10, label %label_9

1:                                                ; No predecessors!
  br label %label_if_10

label_if_10:                                      ; preds = %1, %label_8
  %-t113_1_145 = sub i32 0, 1
  ret i32 %-t113_1_145

2:                                                ; No predecessors!
  br label %label_9

label_9:                                          ; preds = %2, %label_8
  %temp_1_18 = alloca [2 x [2 x i32]], align 4
  %-t114_1_146 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %temp_1_18, i32 0, i32 0, i32 0
  store i32 1, i32* %-t114_1_146, align 4
  %-t115_1_147 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %temp_1_18, i32 0, i32 0, i32 1
  store i32 1, i32* %-t115_1_147, align 4
  %-t116_1_148 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %temp_1_18, i32 0, i32 0, i32 2
  store i32 1, i32* %-t116_1_148, align 4
  %-t117_1_149 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %temp_1_18, i32 0, i32 0, i32 3
  store i32 0, i32* %-t117_1_149, align 4
  %-t119_1_151 = load i32, i32* %-t122_1_154, align 4
  %-t120_1_152 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %temp_1_18, i32 0, i32 0
  %-t121_1_153 = getelementptr [2 x i32], [2 x i32]* %res_1_17, i32 0
  %-t118_1_150 = call i32 @__power(i32 %-t119_1_151, [2 x i32]* %-t120_1_152, [2 x i32]* %-t121_1_153)
  ret i32 %-t118_1_150

3:                                                ; No predecessors!
  ret i32 0
}

define dso_local i32 @error() {
  br label %label_11

label_11:                                         ; preds = %0
  call void @putch(i32 69)
  call void @putch(i32 114)
  call void @putch(i32 114)
  call void @putch(i32 111)
  call void @putch(i32 114)
  call void @putch(i32 33)
  call void @putch(i32 10)
  ret i32 1

1:                                                ; No predecessors!
  ret i32 0
}

define dso_local i32 @out([2 x i32]* %r_1_21) {
  br label %label_12

label_12:                                         ; preds = %0
  %-t123_1_155 = getelementptr [2 x i32], [2 x i32]* %r_1_21, i32 0, i32 0
  %-t124_1_156 = load i32, i32* %-t123_1_155, align 4
  %-t125_1_157 = getelementptr [2 x i32], [2 x i32]* %r_1_21, i32 0, i32 1
  %-t126_1_158 = load i32, i32* %-t125_1_157, align 4
  %-t127_1_159 = getelementptr [2 x i32], [2 x i32]* %r_1_21, i32 1, i32 0
  %-t128_1_160 = load i32, i32* %-t127_1_159, align 4
  %-t129_1_161 = getelementptr [2 x i32], [2 x i32]* %r_1_21, i32 1, i32 1
  %-t130_1_162 = load i32, i32* %-t129_1_161, align 4
  call void @putint(i32 %-t124_1_156)
  call void @putch(i32 32)
  call void @putint(i32 %-t126_1_158)
  call void @putch(i32 10)
  call void @putint(i32 %-t128_1_160)
  call void @putch(i32 32)
  call void @putint(i32 %-t130_1_162)
  call void @putch(i32 10)
  ret i32 1

1:                                                ; No predecessors!
  ret i32 0
}

define dso_local i32 @main() {
  br label %label_13

label_13:                                         ; preds = %0
  %down_1_23 = alloca i32, align 4
  %-t131_1_163 = sdiv i32 0, 10078
  %-t132_1_164 = mul i32 0, 45
  %-t133_1_165 = add i32 %-t131_1_163, %-t132_1_164
  store i32 %-t133_1_165, i32* %down_1_23, align 4
  %n_1_24 = alloca i32, align 4
  %-t134_1_166 = call i32 @getint()
  store i32 %-t134_1_166, i32* %n_1_24, align 4
  %tmp_1_25 = alloca [2 x i32], align 4
  %-t135_1_167 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 0
  store i32 0, i32* %-t135_1_167, align 4
  %-t136_1_168 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 1
  store i32 0, i32* %-t136_1_168, align 4
  %res_1_26 = alloca [2 x [2 x i32]], align 4
  %vec_list_1_27 = alloca [6 x [2 x i32]], align 4
  %-t137_1_169 = sub i32 0, 1
  %-t138_1_170 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 0
  store i32 1, i32* %-t138_1_170, align 4
  %-t139_1_171 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 1
  store i32 0, i32* %-t139_1_171, align 4
  %-t140_1_172 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 2
  store i32 1, i32* %-t140_1_172, align 4
  %-t141_1_173 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 3
  store i32 1, i32* %-t141_1_173, align 4
  %-t142_1_174 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 4
  store i32 2, i32* %-t142_1_174, align 4
  %-t143_1_175 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 5
  store i32 1, i32* %-t143_1_175, align 4
  %-t144_1_176 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 6
  store i32 3, i32* %-t144_1_176, align 4
  %-t145_1_177 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 7
  store i32 1, i32* %-t145_1_177, align 4
  %-t146_1_178 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 8
  store i32 4, i32* %-t146_1_178, align 4
  %-t147_1_179 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 9
  store i32 3, i32* %-t147_1_179, align 4
  %-t148_1_180 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 10
  store i32 %-t137_1_169, i32* %-t148_1_180, align 4
  %-t149_1_181 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 0, i32 11
  store i32 1, i32* %-t149_1_181, align 4
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 110)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 110)
  call void @putch(i32 32)
  call void @putch(i32 45)
  call void @putch(i32 32)
  call void @putch(i32 49)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 43)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 110)
  call void @putch(i32 32)
  call void @putch(i32 45)
  call void @putch(i32 32)
  call void @putch(i32 50)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 40)
  call void @putch(i32 102)
  call void @putch(i32 111)
  call void @putch(i32 114)
  call void @putch(i32 32)
  call void @putch(i32 100)
  call void @putch(i32 105)
  call void @putch(i32 102)
  call void @putch(i32 102)
  call void @putch(i32 101)
  call void @putch(i32 114)
  call void @putch(i32 101)
  call void @putch(i32 110)
  call void @putch(i32 116)
  call void @putch(i32 32)
  call void @putch(i32 105)
  call void @putch(i32 110)
  call void @putch(i32 105)
  call void @putch(i32 116)
  call void @putch(i32 32)
  call void @putch(i32 118)
  call void @putch(i32 97)
  call void @putch(i32 108)
  call void @putch(i32 117)
  call void @putch(i32 101)
  call void @putch(i32 115)
  call void @putch(i32 41)
  call void @putch(i32 10)
  br label %label_for_14

label_for_14:                                     ; preds = %label_13
  br label %label_for_15

1:                                                ; No predecessors!
  br label %label_for_15

label_for_15:                                     ; preds = %label_for_17, %1, %label_for_14
  %-t150_1_182 = icmp ne i32 1, 0
  br i1 %-t150_1_182, label %label_for_16, label %label_for_18

2:                                                ; No predecessors!
  br label %label_for_16

label_for_16:                                     ; preds = %2, %label_for_15
  %-t151_1_183 = load i32, i32* %n_1_24, align 4
  %-t152_1_184 = load i32, i32* %down_1_23, align 4
  %-t153_1_185 = icmp sgt i32 %-t151_1_183, %-t152_1_184
  %-t154_1_186 = zext i1 %-t153_1_185 to i32
  %-t155_1_187 = icmp ne i32 %-t154_1_186, 0
  br i1 %-t155_1_187, label %label_if_20, label %label_else_21

3:                                                ; No predecessors!
  br label %label_else_21

label_else_21:                                    ; preds = %3, %label_for_16
  br label %label_for_18

4:                                                ; No predecessors!
  br label %label_19

5:                                                ; No predecessors!
  br label %label_if_20

label_if_20:                                      ; preds = %5, %label_for_16
  br label %label_22

label_22:                                         ; preds = %label_if_20
  %p_2_28 = alloca i32, align 4
  %-t156_2_188 = call i32 @getint()
  store i32 %-t156_2_188, i32* %p_2_28, align 4
  %ret_2_29 = alloca i32, align 4
  %-t158_2_190 = load i32, i32* %p_2_28, align 4
  %-t159_2_191 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %res_1_26, i32 0, i32 0
  %-t157_2_189 = call i32 @power(i32 %-t158_2_190, [2 x i32]* %-t159_2_191)
  store i32 %-t157_2_189, i32* %ret_2_29, align 4
  %-t160_2_192 = load i32, i32* %p_2_28, align 4
  call void @putch(i32 47)
  call void @putch(i32 42)
  call void @putch(i32 32)
  call void @putch(i32 105)
  call void @putch(i32 110)
  call void @putch(i32 112)
  call void @putch(i32 117)
  call void @putch(i32 116)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t160_2_192)
  call void @putch(i32 32)
  call void @putch(i32 42)
  call void @putch(i32 47)
  call void @putch(i32 10)
  %-t161_2_193 = load i32, i32* %ret_2_29, align 4
  %-t162_2_194 = icmp ne i32 %-t161_2_193, 0
  br i1 %-t162_2_194, label %label_if_24, label %label_23

6:                                                ; No predecessors!
  br label %label_if_24

label_if_24:                                      ; preds = %6, %label_22
  %-t163_2_195 = call i32 @error()
  %-t164_2_196 = icmp ne i32 %-t163_2_195, 0
  br i1 %-t164_2_196, label %label_if_26, label %label_25

7:                                                ; No predecessors!
  br label %label_if_26

label_if_26:                                      ; preds = %7, %label_if_24
  br label %label_27

label_27:                                         ; preds = %label_if_26
  %-t165_3_197 = load i32, i32* %n_1_24, align 4
  %-t166_3_198 = sub i32 %-t165_3_197, 1
  store i32 %-t166_3_198, i32* %n_1_24, align 4
  br label %label_for_17

8:                                                ; No predecessors!
  br label %label_25

label_25:                                         ; preds = %8, %label_if_24
  br label %label_23

label_23:                                         ; preds = %label_25, %label_22
  %-t167_2_199 = load i32, i32* %ret_2_29, align 4
  %-t168_2_200 = icmp ne i32 %-t167_2_199, 0
  br i1 %-t168_2_200, label %label_if_29, label %label_else_30

9:                                                ; No predecessors!
  br label %label_else_30

label_else_30:                                    ; preds = %9, %label_23
  %-t170_2_202 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %res_1_26, i32 0, i32 0
  %-t169_2_201 = call i32 @out([2 x i32]* %-t170_2_202)
  %-t171_2_203 = icmp ne i32 %-t169_2_201, 0
  br i1 %-t171_2_203, label %label_if_32, label %label_31

10:                                               ; No predecessors!
  br label %label_if_32

label_if_32:                                      ; preds = %10, %label_else_30
  br label %label_33

label_33:                                         ; preds = %label_if_32
  %i_3_31 = alloca i32, align 4
  store i32 0, i32* %i_3_31, align 4
  br label %label_for_34

label_for_34:                                     ; preds = %label_33
  br label %label_for_35

11:                                               ; No predecessors!
  br label %label_for_35

label_for_35:                                     ; preds = %label_for_37, %11, %label_for_34
  %-t172_3_204 = icmp ne i32 1, 0
  br i1 %-t172_3_204, label %label_for_36, label %label_for_38

12:                                               ; No predecessors!
  br label %label_for_36

label_for_36:                                     ; preds = %12, %label_for_35
  %-t173_3_205 = load i32, i32* %i_3_31, align 4
  %-t174_3_206 = icmp slt i32 %-t173_3_205, 6
  %-t175_3_207 = zext i1 %-t174_3_206 to i32
  %-t176_3_208 = icmp ne i32 %-t175_3_207, 0
  br i1 %-t176_3_208, label %label_if_40, label %label_else_41

13:                                               ; No predecessors!
  br label %label_else_41

label_else_41:                                    ; preds = %13, %label_for_36
  %-t177_3_209 = icmp ne i32 1, 0
  br i1 %-t177_3_209, label %label_if_43, label %label_else_44

14:                                               ; No predecessors!
  br label %label_else_44

label_else_44:                                    ; preds = %14, %label_else_41
  br label %label_for_38

15:                                               ; No predecessors!
  br label %label_42

16:                                               ; No predecessors!
  br label %label_if_43

label_if_43:                                      ; preds = %16, %label_else_41
  br label %label_45

label_45:                                         ; preds = %label_if_43
  %-t178_4_210 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 0
  %-t179_4_211 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %res_1_26, i32 0, i32 0
  %-t180_4_212 = load i32, i32* %i_3_31, align 4
  %-t181_4_213 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t180_4_212, i32 0
  call void @__vec_mul(i32* %-t178_4_210, [2 x i32]* %-t179_4_211, i32* %-t181_4_213)
  %-t182_4_214 = load i32, i32* %i_3_31, align 4
  %-t183_4_215 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t182_4_214, i32 1
  %-t184_4_216 = load i32, i32* %-t183_4_215, align 4
  %-t185_4_217 = load i32, i32* %i_3_31, align 4
  %-t186_4_218 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t185_4_217, i32 0
  %-t187_4_219 = load i32, i32* %-t186_4_218, align 4
  %-t188_4_220 = load i32, i32* %p_2_28, align 4
  %-t189_4_221 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 1
  %-t190_4_222 = load i32, i32* %-t189_4_221, align 4
  call void @putch(i32 119)
  call void @putch(i32 104)
  call void @putch(i32 101)
  call void @putch(i32 110)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 48)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t184_4_216)
  call void @putch(i32 44)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 49)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t187_4_219)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 61)
  call void @putch(i32 62)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putint(i32 %-t188_4_220)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t190_4_222)
  call void @putch(i32 10)
  %-t191_4_223 = load i32, i32* %i_3_31, align 4
  %-t192_4_224 = add i32 %-t191_4_223, 1
  store i32 %-t192_4_224, i32* %i_3_31, align 4
  %-t193_4_225 = load i32, i32* %i_3_31, align 4
  %-t194_4_226 = icmp sge i32 %-t193_4_225, 6
  %-t195_4_227 = zext i1 %-t194_4_226 to i32
  %-t196_4_228 = icmp ne i32 %-t195_4_227, 0
  br i1 %-t196_4_228, label %label_if_47, label %label_else_48

17:                                               ; No predecessors!
  br label %label_else_48

label_else_48:                                    ; preds = %17, %label_45
  br label %label_for_37

18:                                               ; No predecessors!
  br label %label_46

19:                                               ; No predecessors!
  br label %label_if_47

label_if_47:                                      ; preds = %19, %label_45
  br label %label_for_38

20:                                               ; No predecessors!
  br label %label_46

label_46:                                         ; preds = %20, %18
  br label %label_42

label_42:                                         ; preds = %label_46, %15
  br label %label_39

21:                                               ; No predecessors!
  br label %label_if_40

label_if_40:                                      ; preds = %21, %label_for_36
  br label %label_49

label_49:                                         ; preds = %label_if_40
  %-t197_4_229 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 0
  %-t198_4_230 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %res_1_26, i32 0, i32 0
  %-t199_4_231 = load i32, i32* %i_3_31, align 4
  %-t200_4_232 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t199_4_231, i32 0
  call void @__vec_mul(i32* %-t197_4_229, [2 x i32]* %-t198_4_230, i32* %-t200_4_232)
  %-t201_4_233 = load i32, i32* %i_3_31, align 4
  %-t202_4_234 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t201_4_233, i32 1
  %-t203_4_235 = load i32, i32* %-t202_4_234, align 4
  %-t204_4_236 = load i32, i32* %i_3_31, align 4
  %-t205_4_237 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t204_4_236, i32 0
  %-t206_4_238 = load i32, i32* %-t205_4_237, align 4
  %-t207_4_239 = load i32, i32* %p_2_28, align 4
  %-t208_4_240 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 1
  %-t209_4_241 = load i32, i32* %-t208_4_240, align 4
  call void @putch(i32 119)
  call void @putch(i32 104)
  call void @putch(i32 101)
  call void @putch(i32 110)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 48)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t203_4_235)
  call void @putch(i32 44)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 49)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t206_4_238)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 61)
  call void @putch(i32 62)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putint(i32 %-t207_4_239)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t209_4_241)
  call void @putch(i32 10)
  %-t210_4_242 = load i32, i32* %i_3_31, align 4
  %-t211_4_243 = add i32 %-t210_4_242, 1
  store i32 %-t211_4_243, i32* %i_3_31, align 4
  %-t212_4_244 = load i32, i32* %i_3_31, align 4
  %-t213_4_245 = icmp sge i32 %-t212_4_244, 6
  %-t214_4_246 = zext i1 %-t213_4_245 to i32
  %-t215_4_247 = icmp ne i32 %-t214_4_246, 0
  br i1 %-t215_4_247, label %label_if_51, label %label_else_52

22:                                               ; No predecessors!
  br label %label_else_52

label_else_52:                                    ; preds = %22, %label_49
  br label %label_for_37

23:                                               ; No predecessors!
  br label %label_50

24:                                               ; No predecessors!
  br label %label_if_51

label_if_51:                                      ; preds = %24, %label_49
  br label %label_for_38

25:                                               ; No predecessors!
  br label %label_50

label_50:                                         ; preds = %25, %23
  br label %label_39

label_39:                                         ; preds = %label_50, %label_42
  br label %label_for_37

26:                                               ; No predecessors!
  br label %label_for_37

label_for_37:                                     ; preds = %26, %label_39, %label_else_52, %label_else_48
  br label %label_for_35

27:                                               ; No predecessors!
  br label %label_for_38

label_for_38:                                     ; preds = %27, %label_if_51, %label_if_47, %label_else_44, %label_for_35
  br label %label_31

label_31:                                         ; preds = %label_for_38, %label_else_30
  br label %label_28

28:                                               ; No predecessors!
  br label %label_if_29

label_if_29:                                      ; preds = %28, %label_23
  br label %label_53

label_53:                                         ; preds = %label_if_29
  %i_3_30 = alloca i32, align 4
  store i32 0, i32* %i_3_30, align 4
  br label %label_for_54

label_for_54:                                     ; preds = %label_53
  br label %label_for_55

29:                                               ; No predecessors!
  br label %label_for_55

label_for_55:                                     ; preds = %label_for_57, %29, %label_for_54
  %-t216_3_248 = icmp ne i32 1, 0
  br i1 %-t216_3_248, label %label_for_56, label %label_for_58

30:                                               ; No predecessors!
  br label %label_for_56

label_for_56:                                     ; preds = %30, %label_for_55
  %-t217_3_249 = load i32, i32* %i_3_30, align 4
  %-t218_3_250 = icmp slt i32 %-t217_3_249, 6
  %-t219_3_251 = zext i1 %-t218_3_250 to i32
  %-t220_3_252 = icmp ne i32 %-t219_3_251, 0
  br i1 %-t220_3_252, label %label_if_60, label %label_else_61

31:                                               ; No predecessors!
  br label %label_else_61

label_else_61:                                    ; preds = %31, %label_for_56
  %-t221_3_253 = icmp ne i32 1, 0
  br i1 %-t221_3_253, label %label_if_63, label %label_else_64

32:                                               ; No predecessors!
  br label %label_else_64

label_else_64:                                    ; preds = %32, %label_else_61
  br label %label_for_58

33:                                               ; No predecessors!
  br label %label_62

34:                                               ; No predecessors!
  br label %label_if_63

label_if_63:                                      ; preds = %34, %label_else_61
  br label %label_65

label_65:                                         ; preds = %label_if_63
  %-t222_4_254 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 0
  %-t223_4_255 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %res_1_26, i32 0, i32 0
  %-t224_4_256 = load i32, i32* %i_3_30, align 4
  %-t225_4_257 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t224_4_256, i32 0
  call void @__vec_mul(i32* %-t222_4_254, [2 x i32]* %-t223_4_255, i32* %-t225_4_257)
  %-t226_4_258 = load i32, i32* %i_3_30, align 4
  %-t227_4_259 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t226_4_258, i32 1
  %-t228_4_260 = load i32, i32* %-t227_4_259, align 4
  %-t229_4_261 = load i32, i32* %i_3_30, align 4
  %-t230_4_262 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t229_4_261, i32 0
  %-t231_4_263 = load i32, i32* %-t230_4_262, align 4
  %-t232_4_264 = load i32, i32* %p_2_28, align 4
  %-t233_4_265 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 1
  %-t234_4_266 = load i32, i32* %-t233_4_265, align 4
  call void @putch(i32 119)
  call void @putch(i32 104)
  call void @putch(i32 101)
  call void @putch(i32 110)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 48)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t228_4_260)
  call void @putch(i32 44)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 49)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t231_4_263)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 61)
  call void @putch(i32 62)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putint(i32 %-t232_4_264)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t234_4_266)
  call void @putch(i32 10)
  %-t235_4_267 = load i32, i32* %i_3_30, align 4
  %-t236_4_268 = add i32 %-t235_4_267, 1
  store i32 %-t236_4_268, i32* %i_3_30, align 4
  %-t237_4_269 = load i32, i32* %i_3_30, align 4
  %-t238_4_270 = icmp sge i32 %-t237_4_269, 6
  %-t239_4_271 = zext i1 %-t238_4_270 to i32
  %-t240_4_272 = icmp ne i32 %-t239_4_271, 0
  br i1 %-t240_4_272, label %label_if_67, label %label_else_68

35:                                               ; No predecessors!
  br label %label_else_68

label_else_68:                                    ; preds = %35, %label_65
  br label %label_for_57

36:                                               ; No predecessors!
  br label %label_66

37:                                               ; No predecessors!
  br label %label_if_67

label_if_67:                                      ; preds = %37, %label_65
  br label %label_for_58

38:                                               ; No predecessors!
  br label %label_66

label_66:                                         ; preds = %38, %36
  br label %label_62

label_62:                                         ; preds = %label_66, %33
  br label %label_59

39:                                               ; No predecessors!
  br label %label_if_60

label_if_60:                                      ; preds = %39, %label_for_56
  br label %label_69

label_69:                                         ; preds = %label_if_60
  %-t241_4_273 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 0
  %-t242_4_274 = getelementptr [2 x [2 x i32]], [2 x [2 x i32]]* %res_1_26, i32 0, i32 0
  %-t243_4_275 = load i32, i32* %i_3_30, align 4
  %-t244_4_276 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t243_4_275, i32 0
  call void @__vec_mul(i32* %-t241_4_273, [2 x i32]* %-t242_4_274, i32* %-t244_4_276)
  %-t245_4_277 = load i32, i32* %i_3_30, align 4
  %-t246_4_278 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t245_4_277, i32 1
  %-t247_4_279 = load i32, i32* %-t246_4_278, align 4
  %-t248_4_280 = load i32, i32* %i_3_30, align 4
  %-t249_4_281 = getelementptr [6 x [2 x i32]], [6 x [2 x i32]]* %vec_list_1_27, i32 0, i32 %-t248_4_280, i32 0
  %-t250_4_282 = load i32, i32* %-t249_4_281, align 4
  %-t251_4_283 = load i32, i32* %p_2_28, align 4
  %-t252_4_284 = getelementptr [2 x i32], [2 x i32]* %tmp_1_25, i32 0, i32 1
  %-t253_4_285 = load i32, i32* %-t252_4_284, align 4
  call void @putch(i32 119)
  call void @putch(i32 104)
  call void @putch(i32 101)
  call void @putch(i32 110)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 48)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t247_4_279)
  call void @putch(i32 44)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putch(i32 49)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t250_4_282)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 61)
  call void @putch(i32 62)
  call void @putch(i32 32)
  call void @putch(i32 97)
  call void @putch(i32 91)
  call void @putint(i32 %-t251_4_283)
  call void @putch(i32 93)
  call void @putch(i32 32)
  call void @putch(i32 61)
  call void @putch(i32 32)
  call void @putint(i32 %-t253_4_285)
  call void @putch(i32 10)
  %-t254_4_286 = load i32, i32* %i_3_30, align 4
  %-t255_4_287 = add i32 %-t254_4_286, 1
  store i32 %-t255_4_287, i32* %i_3_30, align 4
  %-t256_4_288 = load i32, i32* %i_3_30, align 4
  %-t257_4_289 = icmp sge i32 %-t256_4_288, 6
  %-t258_4_290 = zext i1 %-t257_4_289 to i32
  %-t259_4_291 = icmp ne i32 %-t258_4_290, 0
  br i1 %-t259_4_291, label %label_if_71, label %label_else_72

40:                                               ; No predecessors!
  br label %label_else_72

label_else_72:                                    ; preds = %40, %label_69
  br label %label_for_57

41:                                               ; No predecessors!
  br label %label_70

42:                                               ; No predecessors!
  br label %label_if_71

label_if_71:                                      ; preds = %42, %label_69
  br label %label_for_58

43:                                               ; No predecessors!
  br label %label_70

label_70:                                         ; preds = %43, %41
  br label %label_59

label_59:                                         ; preds = %label_70, %label_62
  br label %label_for_57

44:                                               ; No predecessors!
  br label %label_for_57

label_for_57:                                     ; preds = %44, %label_59, %label_else_72, %label_else_68
  br label %label_for_55

45:                                               ; No predecessors!
  br label %label_for_58

label_for_58:                                     ; preds = %45, %label_if_71, %label_if_67, %label_else_64, %label_for_55
  br label %label_28

label_28:                                         ; preds = %label_for_58, %label_31
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 47)
  call void @putch(i32 10)
  %-t260_2_292 = load i32, i32* %n_1_24, align 4
  %-t261_2_293 = sub i32 1, %-t260_2_292
  %-t262_2_294 = sub i32 0, %-t261_2_293
  %-t263_2_295 = sub i32 0, %-t262_2_294
  %-t264_2_296 = sub i32 0, %-t263_2_295
  store i32 %-t264_2_296, i32* %n_1_24, align 4
  br label %label_19

label_19:                                         ; preds = %label_28, %4
  br label %label_for_17

46:                                               ; No predecessors!
  br label %label_for_17

label_for_17:                                     ; preds = %46, %label_19, %label_27
  br label %label_for_15

47:                                               ; No predecessors!
  br label %label_for_18

label_for_18:                                     ; preds = %47, %label_else_21, %label_for_15
  call void @putch(i32 47)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 32)
  call void @putch(i32 69)
  call void @putch(i32 78)
  call void @putch(i32 68)
  call void @putch(i32 32)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 42)
  call void @putch(i32 47)
  call void @putch(i32 10)
  ret i32 0
}

; Function Attrs: noinline nounwind optnone ssp uwtable
define i32 @getint() #0 {
  %1 = alloca i32, align 4
  %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str, i64 0, i64 0), i32* %1)
  %3 = load i32, i32* %1, align 4
  ret i32 %3
}

declare i32 @scanf(i8*, ...) #1

; Function Attrs: noinline nounwind optnone ssp uwtable
define i32 @getch() #0 {
  %1 = alloca i8, align 1
  %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i64 0, i64 0), i8* %1)
  %3 = load i8, i8* %1, align 1
  %4 = sext i8 %3 to i32
  ret i32 %4
}

; Function Attrs: noinline nounwind optnone ssp uwtable
define i32 @getarray(i32* %0) #0 {
  %2 = alloca i32*, align 8
  %3 = alloca i32, align 4
  %4 = alloca i32, align 4
  store i32* %0, i32** %2, align 8
  %5 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str, i64 0, i64 0), i32* %3)
  store i32 0, i32* %4, align 4
  br label %6

6:                                                ; preds = %16, %1
  %7 = load i32, i32* %4, align 4
  %8 = load i32, i32* %3, align 4
  %9 = icmp slt i32 %7, %8
  br i1 %9, label %10, label %19

10:                                               ; preds = %6
  %11 = load i32*, i32** %2, align 8
  %12 = load i32, i32* %4, align 4
  %13 = sext i32 %12 to i64
  %14 = getelementptr inbounds i32, i32* %11, i64 %13
  %15 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str, i64 0, i64 0), i32* %14)
  br label %16

16:                                               ; preds = %10
  %17 = load i32, i32* %4, align 4
  %18 = add nsw i32 %17, 1
  store i32 %18, i32* %4, align 4
  br label %6

19:                                               ; preds = %6
  %20 = load i32, i32* %3, align 4
  ret i32 %20
}

; Function Attrs: noinline nounwind optnone ssp uwtable
define void @putint(i32 %0) #0 {
  %2 = alloca i32, align 4
  store i32 %0, i32* %2, align 4
  %3 = load i32, i32* %2, align 4
  %4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str, i64 0, i64 0), i32 %3)
  ret void
}

declare i32 @printf(i8*, ...) #1

; Function Attrs: noinline nounwind optnone ssp uwtable
define void @putch(i32 %0) #0 {
  %2 = alloca i32, align 4
  store i32 %0, i32* %2, align 4
  %3 = load i32, i32* %2, align 4
  %4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.1, i64 0, i64 0), i32 %3)
  ret void
}

; Function Attrs: noinline nounwind optnone ssp uwtable
define void @putarray(i32 %0, i32* %1) #0 {
  %3 = alloca i32, align 4
  %4 = alloca i32*, align 8
  %5 = alloca i32, align 4
  store i32 %0, i32* %3, align 4
  store i32* %1, i32** %4, align 8
  %6 = load i32, i32* %3, align 4
  %7 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str.2, i64 0, i64 0), i32 %6)
  store i32 0, i32* %5, align 4
  br label %8

8:                                                ; preds = %19, %2
  %9 = load i32, i32* %5, align 4
  %10 = load i32, i32* %3, align 4
  %11 = icmp slt i32 %9, %10
  br i1 %11, label %12, label %22

12:                                               ; preds = %8
  %13 = load i32*, i32** %4, align 8
  %14 = load i32, i32* %5, align 4
  %15 = sext i32 %14 to i64
  %16 = getelementptr inbounds i32, i32* %13, i64 %15
  %17 = load i32, i32* %16, align 4
  %18 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.str.3, i64 0, i64 0), i32 %17)
  br label %19

19:                                               ; preds = %12
  %20 = load i32, i32* %5, align 4
  %21 = add nsw i32 %20, 1
  store i32 %21, i32* %5, align 4
  br label %8

22:                                               ; preds = %8
  %23 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @.str.4, i64 0, i64 0))
  ret void
}

; Function Attrs: noinline nounwind optnone ssp uwtable
define void @putstr(i8* %0) #0 {
  %2 = alloca i8*, align 8
  store i8* %0, i8** %2, align 8
  %3 = load i8*, i8** %2, align 8
  %4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str.5, i64 0, i64 0), i8* %3)
  ret void
}

attributes #0 = { noinline nounwind optnone ssp uwtable "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "frame-pointer"="non-leaf" "less-precise-fpmad"="false" "min-legal-vector-width"="0" "no-infs-fp-math"="false" "no-jump-tables"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="apple-a7" "target-features"="+aes,+crypto,+fp-armv8,+neon,+sha2,+zcm,+zcz" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "frame-pointer"="non-leaf" "less-precise-fpmad"="false" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="apple-a7" "target-features"="+aes,+crypto,+fp-armv8,+neon,+sha2,+zcm,+zcz" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}
!llvm.module.flags = !{!1, !2}

!0 = !{!"Homebrew clang version 11.1.0"}
!1 = !{i32 1, !"wchar_size", i32 4}
!2 = !{i32 7, !"PIC Level", i32 2}
