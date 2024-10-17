int func(int x) {
 int n, s = 0; // func_B0
 if (x < 0) {
 n = 0; // func_B1
 } else {
 n = 1; // func_B2
 }

 while (n < 10) { // func_B3
 s = s + n; // func_B4
 n = n + 1;
 }
 return s; // func_B5
}