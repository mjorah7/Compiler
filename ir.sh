clear

#cp testfile.txt testfile.c
#clang testfile.c -o testfile
#./testfile
#echo $?

cp llvm_ir.txt llvm_ir.ll
llvm-link llvm_ir.ll lib.ll -S -o out.ll
lli out.ll
echo ""
echo $?