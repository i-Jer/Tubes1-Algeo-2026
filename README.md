# Kalkulator Aljabar Linier — Tugas Besar 1 IF2123

Tugas Besar 1 IF2123 Aljabar Linier dan Geometri, Semester I 2026/2027.

Program ini adalah pustaka aljabar linier yang ditulis sendiri dalam Java (tanpa pustaka matriks eksternal) beserta program berbasis *command line interface* (CLI) yang memanfaatkannya. Fitur yang tersedia:

| No | Fitur | Metode |
|----|-------|--------|
| 1 | Sistem Persamaan Linier (SPL) | Eliminasi Gauss, Eliminasi Gauss-Jordan (keduanya dengan *partial pivoting*), Matriks Balikan, Kaidah Cramer |
| 2 | Determinan Matriks | Reduksi Baris (OBE), Ekspansi Kofaktor |
| 3 | Matriks Balikan | Augmentasi `[A \| I]` dengan Gauss-Jordan, Adjoin |
| 4 | Interpolasi Polinomial | SPL Vandermonde dengan eliminasi Gauss |
| 5 | Natural Cubic Spline Interpolation | SPL tridiagonal untuk turunan kedua, syarat batas natural |
| 6 | Regresi Spline Kubik | *Truncated power basis* dan persamaan normal `(XᵀX)β = Xᵀy` |

Setiap fitur menampilkan langkah perhitungan (tahapan OBE, matriks antara, substitusi mundur, kofaktor, dan sebagainya), dan hasilnya dapat disimpan ke berkas `.txt`.

## Kelompok Gerobak Pasar Kosambi

| Nama | NIM |
|------|-----|
| Jeremy Gerald Sutanto | 13525104 |
| Christian Immanuel | 13525116 |
| Denzel Santoso | 13525014 |

## Requirements

- Java 17 atau lebih baru (untuk menjalankan JAR)
- Maven 3.6.3 atau lebih baru (hanya untuk kompilasi ulang)

```bash
java --version
mvn --version
```

## Menjalankan program

Berkas JAR siap pakai tersedia di folder `bin`. Dari *root* repositori jalankan:

```bash
java -jar bin/matrix-calculator-1.0-SNAPSHOT.jar
```

Tambahkan opsi `--plain` jika terminal tidak menampilkan warna atau karakter garis dengan benar (misalnya `cmd.exe` lama):

```bash
java -jar bin/matrix-calculator-1.0-SNAPSHOT.jar --plain
```

## Kompilasi ulang

```bash
mvn clean package
```

Berkas JAR (dengan `Main-Class: algeo.App` pada manifest) akan dibuat di `target/matrix-calculator-1.0-SNAPSHOT.jar`. Salin ke folder `bin` untuk mengganti versi yang ada. Program juga dapat dijalankan langsung lewat Maven dengan `mvn compile exec:java`.

## Alur program

1. Program menampilkan **menu utama**:
   1. Sistem Persamaan Linier (SPL)
   2. Determinan Matriks
   3. Matriks Balikan (Invers)
   4. Interpolasi Polinomial
   5. Natural Cubic Spline Interpolation
   6. Regresi Spline Kubik
   7. Keluar
2. Untuk menu 1, 2, 3, 4, dan 6, pilih metode pada **sub-menu** (pilihan terakhir "Kembali" kembali ke menu utama).
3. Pilih sumber masukan: **keyboard** atau **berkas `.txt`** (masukkan *path* berkas, boleh diapit tanda kutip).
4. Program menampilkan metode, masukan, langkah perhitungan, dan hasil.
5. Untuk interpolasi polinomial, *spline*, dan regresi, program meminta nilai `x` yang akan dievaluasi berulang kali; kosongkan masukan untuk selesai.
6. Program menanyakan apakah hasil ingin disimpan ke berkas `.txt`, lalu kembali ke menu utama.

Batas ukuran masukan:

- Keyboard: matriks hingga 11 × 11 (matriks *augmented* SPL hingga 11 × 12).
- Berkas: matriks hingga 1001 × 1001 (matriks *augmented* SPL hingga 1001 × 1002). Langkah perhitungan hanya ditampilkan untuk matriks hingga 11 × 12 agar layar tidak dibanjiri keluaran.
- Interpolasi, *spline*, dan regresi: maksimal 10 titik sampel.

Tanda desimal boleh berupa titik (`3.5`) maupun koma (`3,5`). Keluaran dibulatkan maksimal 3 angka di belakang koma.

## Format berkas masukan

Setiap bilangan dipisahkan *whitespace*; baris kosong di tengah berkas dianggap kesalahan format. Nama berkas tidak memengaruhi cara berkas dibaca.

**SPL** (matriks *augmented* `[A | b]`), contoh `spl_xxx.txt`:

```text
3 4.5 2.8 10 12
-3 7 8.3 11 -4
0.5 -10 -9 12 0
```

**Determinan** dan **matriks balikan** (matriks persegi), contoh `det_xxx.txt` / `inverse_xxx.txt`:

```text
3 4.5 2.8
-3 7 8.3
0.5 -10 -9
```

**Interpolasi, spline, dan regresi** (satu titik `x y` per baris, jumlah titik = jumlah baris), contoh `interpolasi_xxx.txt` / `regresi_xxx.txt`:

```text
0 1
1 2
2 0
3 1
```

Untuk regresi, jumlah dan posisi *knot* dimasukkan lewat keyboard setelah titik sampel dibaca.

## Struktur direktori

```text
.
├── bin
│   └── matrix-calculator-1.0-SNAPSHOT.jar   # JAR siap jalan
├── docs
│   ├── *.pdf                                # laporan dalam format PDF
│   ├── laporan.tex                          # sumber LaTeX laporan
│   ├── foto-kelompok.jpg                    # foto sampul laporan
│   └── ttd-*.png                            # tanda tangan pernyataan integritas
├── src
│   └── main
│       └── java
│           └── algeo
│               ├── App.java                 # titik masuk program (main)
│               ├── cli
│               │   └── CLI.java             # menu utama, sub-menu, alur masukan dan keluaran
│               ├── io
│               │   ├── ConsoleInput.java    # masukan keyboard yang tervalidasi
│               │   ├── FileIO.java          # baca matriks/titik dari .txt, tulis hasil, parsing angka
│               │   ├── InputEndException.java  # penanda masukan berakhir (EOF)
│               │   └── Style.java           # tampilan CLI (banner, menu, warna, mode --plain)
│               ├── matrix
│               │   └── Matrix.java          # struktur data matriks, OBE, operasi aljabar dasar
│               ├── spl
│               │   ├── Elimination.java     # eselon baris (tereduksi) dengan partial pivoting
│               │   ├── SPLResult.java       # jenis solusi dan bentuk parametrik
│               │   └── SPLSolver.java       # Gauss, Gauss-Jordan, matriks balikan, Cramer
│               ├── determinant
│               │   └── Determinant.java     # reduksi baris (OBE) dan ekspansi kofaktor
│               ├── inverse
│               │   └── Inverse.java         # augmentasi [A | I] dan metode adjoin
│               ├── interpolasi
│               │   ├── Interpolasi.java     # interpolasi polinomial (SPL Vandermonde)
│               │   └── NaturalCubicSpline.java  # natural cubic spline (SPL tridiagonal)
│               ├── regression
│               │   └── SplineRegression.java    # regresi spline kubik (truncated power basis)
│               └── modules
│                   └── ModuleContoh.java    # contoh bawaan templat (tidak dipakai)
├── test                                     # berkas masukan kasus uji (lihat tabel di bawah)
├── pom.xml                                  # konfigurasi Maven
└── README.md
```

## Kasus uji

Folder `test` memuat berkas masukan untuk seluruh kasus uji pada Bab 5 spesifikasi:

| Berkas | Kasus |
|--------|-------|
| `det_5_1_1.txt` – `det_5_1_3.txt` | 5.1 Determinan |
| `inverse_5_2_1.txt`, `inverse_5_2_2.txt` | 5.2 Matriks balikan |
| `spl_5_3_1.txt` – `spl_5_3_3.txt`, `spl_5_3_4_n6.txt`, `spl_5_3_4_n10.txt` | 5.3 SPL `Ax = b` |
| `spl_5_4_1.txt`, `spl_5_4_2.txt` | 5.4 SPL matriks *augmented* |
| `spl_5_5_1.txt`, `spl_5_5_2.txt` | 5.5 SPL bentuk umum |
| `spl_5_6.txt` | 5.6 Aplikasi SPL (persamaan normal regresi linier) |
| `interpolasi_5_7a.txt`, `interpolasi_5_7b.txt` | 5.7 Interpolasi polinomial |
| `interpolasi_5_8.txt` | 5.8 Natural cubic spline |
| `regresi_5_9.txt` | 5.9 Regresi spline (knot di `x = 0`) |
| `spl_hilbert_n6.txt`, `spl_hilbert_n10.txt` | Eksperimen tambahan: matriks Hilbert |
| `spl_acak_1001.txt`, `det_acak_1001.txt` | Eksperimen tambahan: batas ukuran berkas 1001 × 1001 |

## Laporan

Laporan berada di folder `docs`, beserta sumber LaTeX-nya (`docs/laporan.tex`).
