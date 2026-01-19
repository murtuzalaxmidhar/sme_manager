package com.lax.sme_manager.ui.view;

import com.lax.sme_manager.repository.model.PurchaseEntity;
import com.lax.sme_manager.ui.theme.LaxTheme;
import com.lax.sme_manager.util.i18n.AppLabel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;

/**
 * Modern, Professional "Digital Receipt" Purchase Detail View.
 * Refined with horizontal Label : Value layout and streamlined fields.
 */
public class PurchaseDetailView extends ScrollPane {

        private final PurchaseEntity purchase;
        private final String vendorName;

        public PurchaseDetailView(PurchaseEntity purchase, String vendorName) {
                this.purchase = purchase;
                this.vendorName = vendorName;
                initializeUI();
        }

        private void initializeUI() {
                setFitToWidth(true);
                setStyle("-fx-background-color: #f8fafc;");
                setPadding(new Insets(24));

                VBox content = new VBox(0);
                content.setAlignment(Pos.TOP_CENTER);

                // --- Paper Card Container ---
                VBox paper = new VBox(0);
                paper.setMaxWidth(700);
                paper.setStyle(
                                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
                paper.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.08)));

                // --- Header Section ---
                VBox header = new VBox(8);
                header.setPadding(new Insets(32));
                header.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

                Label lblTitle = new Label(AppLabel.TITLE_PURCHASE_DETAILS.get());
                lblTitle.setStyle(
                                "-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: "
                                                + LaxTheme.Colors.TEXT_PRIMARY + ";");
                header.getChildren().add(lblTitle);

                // --- Body Section ---
                VBox body = new VBox(24);
                body.setPadding(new Insets(32));

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");

                // Main Fields List (Vertical stack of horizontal pairs)
                VBox fieldStack = new VBox(12);

                addField(fieldStack, AppLabel.LBL_PURCHASE_DATE.get(), purchase.getEntryDate().format(dtf));
                addField(fieldStack, AppLabel.LBL_VENDOR.get(), vendorName);
                addField(fieldStack, AppLabel.LBL_BAGS.get(), String.valueOf(purchase.getBags()));
                addField(fieldStack, AppLabel.LBL_RATE.get(), String.format("₹%.2f", purchase.getRate()));
                addField(fieldStack, AppLabel.LBL_WEIGHT.get(), String.format("%.2f kg", purchase.getWeightKg()));
                addField(fieldStack, AppLabel.LBL_LUMPSUM.get(), purchase.getIsLumpsum() ? "Yes" : "No");
                addField(fieldStack, AppLabel.LBL_STATUS.get(), purchase.getStatus());
                addField(fieldStack, AppLabel.LBL_PAYMENT_MODE.get(), purchase.getPaymentMode());
                addField(fieldStack, AppLabel.LBL_PAID_IN_ADVANCE.get(),
                                purchase.getAdvancePaid() != null && purchase.getAdvancePaid() ? "Yes" : "No");

                if ("CHEQUE".equals(purchase.getPaymentMode())) {
                        addField(fieldStack, "Cheque No",
                                        purchase.getChequeNumber() != null ? purchase.getChequeNumber() : "-");
                        addField(fieldStack, "Cheque Date",
                                        purchase.getChequeDate() != null ? purchase.getChequeDate().format(dtf) : "-");
                }

                body.getChildren().add(fieldStack);
                body.getChildren().add(new Separator());

                // Financials Section
                VBox finSection = new VBox(0);
                finSection.setStyle(
                                "-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

                VBox finRows = new VBox(10);
                finRows.setPadding(new Insets(20));

                addFinRow(finRows, AppLabel.LBL_AMOUNT.get() + " (Base)",
                                String.format("₹%.2f", purchase.getBaseAmount()),
                                false);
                addFinRow(finRows, "Market Fee (" + purchase.getMarketFeePercent() + "%)",
                                String.format("+ ₹%.2f", purchase.getMarketFeeAmount()), false);
                addFinRow(finRows, "Commission (" + purchase.getCommissionPercent() + "%)",
                                String.format("+ ₹%.2f", purchase.getCommissionFeeAmount()), false);

                // Gradient Total Bar
                HBox totalBar = new HBox();
                totalBar.setPadding(new Insets(16, 20, 16, 20));
                totalBar.setStyle("-fx-background-color: " + LaxTheme.Colors.PRIMARY_TEAL
                                + "; -fx-background-radius: 0 0 8 8;");
                totalBar.setAlignment(Pos.CENTER_RIGHT);

                Label lblTotalTxt = new Label(AppLabel.LBL_TOTAL.get());
                lblTotalTxt.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 14; -fx-font-weight: bold;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblTotalVal = new Label(String.format("₹%.2f", purchase.getGrandTotal()));
                lblTotalVal.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold;");

                totalBar.getChildren().addAll(lblTotalTxt, spacer, lblTotalVal);

                finSection.getChildren().addAll(finRows, totalBar);
                body.getChildren().add(finSection);

                // Notes if any
                if (purchase.getNotes() != null && !purchase.getNotes().isEmpty()) {
                        VBox notesBox = new VBox(6);
                        Label nTitle = new Label(AppLabel.LBL_NOTES.get());
                        nTitle.setStyle(
                                        "-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #64748b; -fx-text-transform: uppercase;");
                        Label nVal = new Label(purchase.getNotes());
                        nVal.setStyle("-fx-font-size: 14; -fx-text-fill: #334155;");
                        nVal.setWrapText(true);
                        notesBox.getChildren().addAll(nTitle, nVal);
                        body.getChildren().add(notesBox);
                }

                paper.getChildren().addAll(header, body);
                content.getChildren().add(paper);
                setContent(content);
        }

        private void addField(VBox parent, String label, String value) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);

                Label lblLabel = new Label(label + " :");
                lblLabel.setPrefWidth(160);
                lblLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14; -fx-font-weight: 500;");

                Label lblValue = new Label(value);
                lblValue.setStyle("-fx-text-fill: " + LaxTheme.Colors.TEXT_PRIMARY
                                + "; -fx-font-size: 14; -fx-font-weight: bold;");

                row.getChildren().addAll(lblLabel, lblValue);
                parent.getChildren().add(row);
        }

        private void addFinRow(VBox parent, String label, String value, boolean isTotal) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);

                Label lblLabel = new Label(label);
                lblLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblValue = new Label(value);
                lblValue.setStyle("-fx-text-fill: #334155; -fx-font-size: 14; -fx-font-weight: bold;");

                row.getChildren().addAll(lblLabel, spacer, lblValue);
                parent.getChildren().add(row);
        }
}
