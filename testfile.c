//79_var_name.sy
int main() {
  int a = 2;
  const int b = 20;
  int c[b] = {1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  int d = 0;
  for(;a < b;) {
    c[a] = c[a] + c[a - 1] + c[a - 2];
    d = d + c[a];
    printf("%d", c[a]);
    printf("%d", 10);
    a = a + 1;
  }
	printf("%d", d);
  return d;
}
