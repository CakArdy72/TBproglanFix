package com.example.tbproglan;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The type Aplikasi kasir.
 */
public class AplikasiKasir extends Application {
    private int totalBelanja = 0;
    private int nomorTransaksi = 1;
    private Map<String, Integer> keranjang = new HashMap<>();
    private Map<String, Integer> menu = new HashMap<>();

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        bacaMenuDariFile("menu.txt"); // Membaca menu dari file

        primaryStage.setTitle("Aplikasi Kasir");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Komponen UI
        Label itemLabel = new Label("Pilih Barang:");
        ComboBox<String> itemComboBox = new ComboBox<>();
        itemComboBox.getItems().addAll(menu.keySet());

        Label quantityLabel = new Label("Jumlah:");
        Spinner<Integer> quantitySpinner = new Spinner<>(0, 30, 1); // Maksimal 30 stok

        Button addToCartButton = new Button("Tambah ke Keranjang");
        TextArea receiptTextArea = new TextArea();
        receiptTextArea.setEditable(false);

        Label totalLabel = new Label("Total Belanja:");

        TextField totalTextField = new TextField();
        totalTextField.setEditable(false);

        Label bayarLabel = new Label("Bayar:");

        TextField bayarTextField = new TextField();

        Button payButton = new Button("Bayar");

        Button printButton = new Button("Print Struk");

        Button resetButton = new Button("Reset Transaksi");

        // Menambahkan komponen ke grid
        grid.add(itemLabel, 0, 0);
        grid.add(itemComboBox, 1, 0);
        grid.add(quantityLabel, 0, 1);
        grid.add(quantitySpinner, 1, 1);
        grid.add(addToCartButton, 1, 2);
        grid.add(receiptTextArea, 0, 3, 3, 1);
        grid.add(totalLabel, 0, 4);
        grid.add(totalTextField, 1, 4);
        grid.add(bayarLabel, 0, 5);
        grid.add(bayarTextField, 1, 5);
        grid.add(payButton, 1, 6);
        grid.add(printButton, 2, 6);
        grid.add(resetButton,2,0);

        // Event handling
        addToCartButton.setOnAction(e -> {
            int jumlah = quantitySpinner.getValue();
            if (jumlah + jumlahDalamKeranjang() > 10) {
                peringatan("Jumlah barang maksimal 10 stok/barang! Sudah mencapai batas maksimal Jumlah per orang!");
            } else if (jumlah + jumlahDalamKeranjang() <= 0) {
                peringatan("Jumlah barang minimal 1 stok/barang!");
            } else {
                tambahKeKeranjang(itemComboBox.getValue(), jumlah, receiptTextArea);
                itemComboBox.getSelectionModel().clearSelection();
                quantitySpinner.getValueFactory().setValue(1);
                totalTextField.setText(Integer.toString(totalBelanja));
            }
        });

        payButton.setOnAction(e -> {
            int bayar = Integer.parseInt(bayarTextField.getText());
            if (bayar >= totalBelanja) {
                int kembalian = bayar - totalBelanja;
                if (bayar > 500000) {
                    peringatan("Maaf, jumlah uang yang Anda masukkan melebihi batas maksimal.");
                } else {
                    tampilkanStruk(receiptTextArea, totalBelanja, bayar, kembalian);
                    peringatan("Uang kembalian Anda adalah: " + kembalian);
                    simpanStrukKeFile(receiptTextArea.getText());
                    nomorTransaksi++;
                    resetTransaksi();
                }
            } else {
                peringatan("Uang yang Anda masukkan kurang!");
            }
        });


        printButton.setOnAction(e -> {
            printStruk(receiptTextArea.getText());
        });

        resetButton.setOnAction(e -> {
            resetTransaksi();
            receiptTextArea.clear();
            totalTextField.clear();
            bayarTextField.clear();
            nomorTransaksi = 1;
        });

        Scene scene = new Scene(grid, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void bacaMenuDariFile(String namaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(namaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String namaBarang = parts[0];
                    int harga = Integer.parseInt(parts[1]);
                    menu.put(namaBarang, harga);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tambahKeKeranjang(String barang, int jumlah, TextArea receiptTextArea) {
        int hargaBarang = hitungTotalHarga(barang, jumlah);
        totalBelanja += hargaBarang;

        keranjang.put(barang, keranjang.getOrDefault(barang, 0) + jumlah);

        String struk = "Transaksi #" + nomorTransaksi + "\nBarang: " + barang +
                "\nJumlah: " + jumlah + "\nTotal Harga: " + hargaBarang + "\n\n";
        receiptTextArea.appendText(struk);
    }

    private int hitungTotalHarga(String barang, int jumlah) {
        return menu.getOrDefault(barang, 0) * jumlah;
    }

    private int jumlahDalamKeranjang() {
        // Hitung jumlah total barang dalam keranjang
        return keranjang.values().stream().mapToInt(Integer::intValue).sum();
    }

    private void tampilkanStruk(TextArea receiptTextArea, int totalBelanja, int bayar, int kembalian) {
        String struk = receiptTextArea.getText() +
                "Total Pembayaran: " + totalBelanja +
                "\nBayar: " + bayar +
                "\nKembalian: " + kembalian +
                "\n---------------------------\n";
        receiptTextArea.setText(struk);
    }

    private void printStruk(String struk) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("struk_pembayaran.txt", true))) {
            writer.write(struk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void simpanStrukKeFile(String struk) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String namaFile = "Struk_transaksi_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(namaFile))) {
            writer.write(struk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetTransaksi() {
        totalBelanja = 0;
        keranjang.clear();
    }

    private void peringatan(String pesan) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }
}
