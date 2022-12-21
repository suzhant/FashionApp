package com.sushant.fashionapp.Utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
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
import com.sushant.fashionapp.Models.Address;
import com.sushant.fashionapp.Models.Cart;
import com.sushant.fashionapp.Models.Order;
import com.sushant.fashionapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    public static File createPdf(Context context, Address address, String receiverMail, Order order) throws FileNotFoundException, MalformedURLException {

        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(filepath, "FashionApp Invoice-" + System.nanoTime() + ".pdf");

        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

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
        table.addCell(new Cell().add(new Paragraph("#123")).setBorder(Border.NO_BORDER));

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
        float[] columnWidth1 = {50f, 250f, 70f, 70f, 90f};
        Table table1 = new Table(columnWidth1);

        DeviceRgb skyBlue = new DeviceRgb(0, 174, 163);
        DeviceRgb grey = new DeviceRgb(239, 239, 239);

        //table-2, Row- 1
        Text column1 = new Text("S.N");
        column1.setFontColor(ColorConstants.WHITE);
        Text column2 = new Text("Item Description");
        column2.setFontColor(ColorConstants.WHITE);
        Text column3 = new Text("Quantity");
        column3.setFontColor(ColorConstants.WHITE);
        Text column4 = new Text("Unit price");
        column4.setFontColor(ColorConstants.WHITE);
        Text column5 = new Text("Amount");
        column5.setFontColor(ColorConstants.WHITE);

        table1.addCell(new Cell().add(new Paragraph(column1)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column2)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column3)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column4)).setBackgroundColor(skyBlue));
        table1.addCell(new Cell().add(new Paragraph(column5)).setBackgroundColor(skyBlue));


        int i = 1;
        int subtotal = 0;
        for (Cart product : order.getProducts()) {
            table1.addCell(new Cell().add(new Paragraph(String.valueOf(i))).setBackgroundColor(grey));
            table1.addCell(new Cell().add(new Paragraph(product.getpName())).setBackgroundColor(grey));
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
        table1.addCell(new Cell(3, 3).add(new Paragraph("")).setBorder(Border.NO_BORDER));
        //   table1.addCell(new Cell().add(new Paragraph("")));
        //    table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("SubTotal: ")));
        table1.addCell(new Cell().add(new Paragraph(String.valueOf(subtotal))));

        //table-2, Row- 7
        //   table1.addCell(new Cell(1,3).add(new Paragraph("")));
        //   table1.addCell(new Cell().add(new Paragraph("")));
        //   table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("Tax:")));
        table1.addCell(new Cell().add(new Paragraph("0.0%")));

        //table-2, Row- 8
        //  table1.addCell(new Cell(1,3).add(new Paragraph("")));
        //  table1.addCell(new Cell().add(new Paragraph("")));
        //  table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("Total:").setBold()));
        table1.addCell(new Cell().add(new Paragraph(MessageFormat.format("Rs. {0}", subtotal)).setBold()));


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


        Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show();
        return file;
    }

    private static Cell createImageCell(byte[] path) throws MalformedURLException {
        Image img = new Image(ImageDataFactory.create(path));
        img.setWidth(UnitValue.createPercentValue(100));
        img.setHeight(UnitValue.createPercentValue(100));
        Cell cell = new Cell().add(img);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }


}
