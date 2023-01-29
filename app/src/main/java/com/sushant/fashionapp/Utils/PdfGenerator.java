package com.sushant.fashionapp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sushant.fashionapp.Buyer.OrderFinalPageActivity;
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Invoice;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.Models.Store;
import com.sushant.fashionapp.R;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

public class PdfGenerator {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    Context context;
    Order order;
    Address address;
    String receiverMail;
    String invoiceNo;
    long date;

    public PdfGenerator(Context context, Order order, Address address, String receiverMail, String invoiceNo, long date) {
        this.context = context;
        this.order = order;
        this.address = address;
        this.receiverMail = receiverMail;
        this.invoiceNo = invoiceNo;
        this.date = date;
    }


    public File createInvoicePdf() throws IOException {

        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(filepath, "FashionApp Invoice-" + System.nanoTime() + ".pdf");

        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        WriterProperties properties = new WriterProperties();
        properties.setPdfVersion(PdfVersion.PDF_2_0);
        properties.useSmartMode();
        properties.setCompressionLevel(CompressionConstants.BEST_COMPRESSION);

        float[] columnWidth = {150, 140, 140, 150};
        Table table = new Table(columnWidth);
        table.setBorder(Border.NO_BORDER);


        Text text = new Text("INVOICE");
        text.setFontSize(30f);
        text.setBold();


        //getting drawable and converting into bitmap
        byte[] bytes = ImageUtils.getByteFromDrawable(context, R.drawable.fashion_logo);

        //1st row
        table.addCell(new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(createImageCell(bytes));


        //2nd row

        table.addCell(new Cell().add(new Paragraph("Fashion App")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        //3rd row

        table.addCell(new Cell().add(new Paragraph("Dhobighat,Lalitpur")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));


        //4th row
        table.addCell(new Cell().add(new Paragraph("+977 9880948607")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("Payment Method").setBold()).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        //5th row
        table.addCell(new Cell().add(new Paragraph("sushantshrestha62@gmail.com")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(order.getPaymentMethod())).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        //6th row
        table.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        //7th row
        Text text2 = new Text("Bill to");
        text2.setBold();
        Text text3 = new Text("Invoice Details");
        text3.setBold();
        table.addCell(new Cell().add(new Paragraph(text2)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(text3)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));


        //8
        table.addCell(new Cell().add(new Paragraph(address.getName())).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("Invoice No:")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("#" + invoiceNo)).setBorder(Border.NO_BORDER));

        //9
        table.addCell(new Cell().add(new Paragraph(address.getStreetAddress() + "," + address.getCity())).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("Invoice Date:")).setBorder(Border.NO_BORDER));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(order.getOrderDate()), ZoneId.systemDefault());
            table.addCell(new Cell().add(new Paragraph(dateTime.format(formatter))).setBorder(Border.NO_BORDER));
        }

        //10
        table.addCell(new Cell().add(new Paragraph(address.getMobile())).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("Order ID:")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph(order.getOrderId())).setBorder(Border.NO_BORDER));

        //11
        table.addCell(new Cell().add(new Paragraph(receiverMail)).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));


        //2nd table
        float[] columnWidth1 = {50f, 250f, 50f, 70f, 70f, 90f};
        Table table1 = new Table(columnWidth1);

        DeviceRgb skyBlue = new DeviceRgb(0, 174, 163);
        DeviceRgb grey = new DeviceRgb(239, 239, 239);

        //table-2, Row- 1
        Text column1 = new Text("S.N");
        column1.setFontColor(ColorConstants.WHITE);
        Text column2 = new Text("Item Description");
        column2.setFontColor(ColorConstants.WHITE);
        Text column3 = new Text("Size");
        column3.setFontColor(ColorConstants.WHITE);
        Text column4 = new Text("Quantity");
        column4.setFontColor(ColorConstants.WHITE);
        Text column5 = new Text("Unit price");
        column5.setFontColor(ColorConstants.WHITE);
        Text column6 = new Text("Amount");
        column6.setFontColor(ColorConstants.WHITE);

        table1.addCell(new Cell().add(new Paragraph(column1)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column2)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column3)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column4)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column5)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column6)).setBackgroundColor(skyBlue));


        int i = 1;
        int subtotal = 0;
        for (Cart product : order.getProducts()) {
            table1.addCell(new Cell().add(new Paragraph(String.valueOf(i))).setBackgroundColor(grey));
            table1.addCell(new Cell().add(new Paragraph(product.getpName())).setBackgroundColor(grey));
            table1.addCell(new Cell().add(new Paragraph(product.getSize())).setBackgroundColor(grey));
            table1.addCell(new Cell().add(new Paragraph(product.getQuantity().toString())).setBackgroundColor(grey));
            int price;
            if (product.getBargainPrice() != null) {
                price = product.getBargainPrice();
            } else {
                price = product.getpPrice();
            }
            table1.addCell(new Cell().add(new Paragraph(String.valueOf(price))).setBackgroundColor(grey));
            int amount = price * product.getQuantity();
            subtotal = subtotal + amount;
            table1.addCell(new Cell().add(new Paragraph(String.valueOf(amount))).setBackgroundColor(grey));
            i++;
        }

        //table-2, Row- 6
        table1.addCell(new Cell(4, 4).add(new Paragraph("")).setBorder(Border.NO_BORDER));
        //   table1.addCell(new Cell().add(new Paragraph("")));
        //    table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("SubTotal: ")));
        table1.addCell(new Cell().add(new Paragraph(String.valueOf(subtotal))));

        //table-2, Row- 7
        //   table1.addCell(new Cell(1,3).add(new Paragraph("")));
        //   table1.addCell(new Cell().add(new Paragraph("")));
        //   table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("Delivery:")));
        int deliveryCharge = 0;
        for (Store store : order.getStores()) {
            deliveryCharge = deliveryCharge + store.getDeliveryCharge();
        }
        table1.addCell(new Cell().add(new Paragraph(String.valueOf(deliveryCharge))));

        //table-2, Row- 8
        //  table1.addCell(new Cell(1,3).add(new Paragraph("")));
        //  table1.addCell(new Cell().add(new Paragraph("")));
        //  table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("Total:").setBold()));
        table1.addCell(new Cell().add(new Paragraph(MessageFormat.format("Rs. {0}", subtotal + deliveryCharge)).setBold()));


        List list = new List();
        list.add("You can return items within 7 days.");
        list.add("Change of mind is not applicable.");


        document.add(table);
        document.add(new Paragraph("\n\n"));
        document.add(table1);
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Terms and Conditions").setBold());
        document.add(list);
        document.add(new Paragraph("\n\n\n"));
        document.add(new Paragraph("Thank You").setBold().setFontSize(20f).setTextAlignment(TextAlignment.CENTER));
        document.close();
        pdfWriter.close();


        Toast.makeText(context, "Invoice Created", Toast.LENGTH_SHORT).show();
        return file;
    }


    private static Cell createImageCell(byte[] path) {
        Image img = new Image(ImageDataFactory.create(path));
        img.setWidth(UnitValue.createPercentValue(100));
        img.setHeight(UnitValue.createPercentValue(100));
        Cell cell = new Cell().add(img);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }

    public void uploadInvoice(File document, ProgressDialog dialog) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Invoice").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        UploadTask uploadTask = reference.putFile(Uri.fromFile(document));
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            Invoice invoice = new Invoice();
                            invoice.setInvoiceNo(invoiceNo);
                            invoice.setInvoiceDate(date);
                            invoice.setInvoiceUrl(filePath);
                            invoice.setOrderId(order.getOrderId());
                            database.getReference().child("Invoice").child(invoiceNo).setValue(invoice).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(context, OrderFinalPageActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                }
                            });
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

}
