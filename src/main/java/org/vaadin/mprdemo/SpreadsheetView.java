package org.vaadin.mprdemo;

import static org.reflections.util.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.CellValueChangeListener;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.DataLabels;
import com.vaadin.flow.component.charts.model.DataSeries;
import com.vaadin.flow.component.charts.model.DataSeriesItem;
import com.vaadin.flow.component.charts.model.Legend;
import com.vaadin.flow.component.charts.model.PlotOptionsColumn;
import com.vaadin.flow.component.charts.model.PlotOptionsPie;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.mpr.LegacyWrapper;

@Route(value = SpreadsheetView.ROUTE, layout = MyUI.class)
public class SpreadsheetView extends VerticalLayout {
    public static final String ROUTE = "spreadsheet";
    public static final String TITLE = "Spreadsheet";    

    private HorizontalLayout chartsLayout;
    private Collection<Chart> charts;
    private Spreadsheet spreadsheet;

    public SpreadsheetView() {
        setSizeFull();
        setSpacing(false);
        setMargin(false);

        initSpreadsheet();
        initCharts();
        LegacyWrapper spreadsheetWrapper = new LegacyWrapper(spreadsheet);
        spreadsheetWrapper.setHeight("50%");
        spreadsheetWrapper.setWidth("100%");
        chartsLayout.setHeight("50%");
        add(chartsLayout, spreadsheetWrapper);
    }

    private void initCharts() {
        chartsLayout = new HorizontalLayout();
        chartsLayout.setSpacing(false);
        chartsLayout.setSizeFull();
        charts = new ArrayList<>();
        charts.add(createPieChart());
        charts.add(createColumnChart());
        for (Chart chart : charts) {
        	chart.setWidth("50%");
            chartsLayout.add(chart);
        }
        updateChartsData();
    }

    private Chart createPieChart() {
        Chart chart = new Chart(ChartType.PIE);
        chart.setSizeFull();
        Configuration conf = chart.getConfiguration();
        conf.getTooltip().setEnabled(false);
        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAnimation(false);
        DataLabels labels = new DataLabels();
        labels.setEnabled(true);
        labels.setFormatter(
                "''+ this.point.name +': '+ this.percentage.toFixed(2) +' %'");
        plotOptions.setDataLabels(labels);
        conf.setPlotOptions(plotOptions);
        return chart;
    }

    private Chart createColumnChart() {
        Chart chart = new Chart(ChartType.COLUMN);
        chart.setSizeFull();
        Configuration conf = chart.getConfiguration();
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        conf.setPlotOptions(plotOptions);
        Legend legend = new Legend();
        legend.setEnabled(false);
        conf.setLegend(legend);

        return chart;
    }

    private void initSpreadsheet() {
        spreadsheet = new Spreadsheet();
        spreadsheet.addCellValueChangeListener(new CellValueChangeListener() {
            @Override
            public void onCellValueChange(CellValueChangeEvent event) {
                updateChartsData();
            }
        });
        CellStyle backgroundColorStyle = spreadsheet.getWorkbook()
                .createCellStyle();
        backgroundColorStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
        Cell cell = spreadsheet.createCell(0, 0,
                "Edit this spreadsheet to alter chart title and data");
        cell.setCellStyle(backgroundColorStyle);

        for (int i = 1; i <= 3; i++) {
            cell = spreadsheet.createCell(0, i, "");
            cell.setCellStyle(backgroundColorStyle);
        }

        spreadsheet.createCell(1, 0, "This is chart title");
        spreadsheet.createCell(2, 0, "Category");
        spreadsheet.createCell(2, 1, "Amount");
        spreadsheet.createCell(3, 0, "Brand 1");
        spreadsheet.createCell(3, 1, 90d);
        spreadsheet.createCell(4, 0, "Brand 2");
        spreadsheet.createCell(4, 1, 7d);
        spreadsheet.createCell(5, 0, "Brand 3");
        spreadsheet.createCell(5, 1, 3d);
        spreadsheet.setColumnWidth(0, 130);
        spreadsheet.setSizeFull();
        
        spreadsheet.refreshCells(cell);
    }

    private void updateChartsData() {
        for (Chart chart : charts) {
            updateChartData(chart);
        }
    }

    private void updateChartData(Chart chart) {
        int rowIndex = 3;
        Configuration conf = chart.getConfiguration();
        XAxis xAxis = conf.getxAxis();
        String oldTitle = conf.getTitle().getText();
        String newTitle = getStringValue(1, 0);
        conf.setTitle(newTitle);
        DataSeries oldSeries = null;
        if (!conf.getSeries().isEmpty()) {
            oldSeries = (DataSeries) conf.getSeries().get(0);
        }
        DataSeries series = new DataSeries();
        Collection<String> categories = new ArrayList<>();
        while (!isEmpty(getStringValue(rowIndex, 0))) {
            series.add(new DataSeriesItem(getStringValue(rowIndex, 0),
                    getNumericValue(rowIndex, 1)));
            categories.add(getStringValue(rowIndex, 0));
            rowIndex++;
        }
        if (oldSeries == null || !series.toString().equals(oldSeries.toString())
                || !newTitle.equals(oldTitle)) {
            conf.setSeries(series);
            xAxis.setCategories(categories.toArray(new String[] {}));
            chart.drawChart();
        }
    }

    private String getStringValue(int rowIndex, int columnIndex) {
        Cell cell = spreadsheet.getCell(rowIndex, columnIndex);
        if (cell != null) {
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue();
        }
        return null;
    }

    private Double getNumericValue(int rowIndex, int columnIndex) {
        Cell cell = spreadsheet.getCell(rowIndex, columnIndex);
        if (cell != null && (cell.getCellType() == CellType.NUMERIC
                || (cell.getCellType() == CellType.FORMULA && cell
                        .getCachedFormulaResultType() == CellType.NUMERIC))) {
            return cell.getNumericCellValue();
        }
        return 0d;
    }	
	
}
