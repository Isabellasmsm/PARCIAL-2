import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import datechooser.beans.DateChooserCombo;
import entidades.TemperaturaCiudad;
import servicios.TemperaturaCiudadServicio;


public class FrmTemperaturasCiudades extends JFrame {

    private DateChooserCombo dccDesde, dccHasta;
    private JTabbedPane tpTemperaturasCiudad;
    private JPanel pnlGrafica;
    private JPanel pnlEstadisticas;
    private JPanel pnlTemperaturasExtremas;
    private List<TemperaturaCiudad> datos;

    public FrmTemperaturasCiudades() {

        setTitle("Temperaturas en las principales ciudades del país.");
        setSize(700, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnGraficar.setToolTipText("Grafica Ciudad vs Promedio");
        btnGraficar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnGraficarClick();
            }
        });
        tb.add(btnGraficar);

        JButton btnCalcularEstadisticas = new JButton();
        btnCalcularEstadisticas.setIcon(new ImageIcon(getClass().getResource("/iconos/Datos.png")));
        btnCalcularEstadisticas.setToolTipText("Promedio de la temperatura en cada ciudad a partir de una fecha.");
        btnCalcularEstadisticas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCalcularPromediosTemperaturasClick();
            }
        });
        tb.add(btnCalcularEstadisticas);

        JButton btnMaxMin = new JButton();
        btnMaxMin.setIcon(new ImageIcon(getClass().getResource("/iconos/Temperatura.png")));
        btnMaxMin.setToolTipText("Ciudades con temperaturas extremas.");
        btnMaxMin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCalcularCiudadesTemperaturasExtremasClick();
            }
        });
        tb.add(btnMaxMin);

        
        JPanel pnlCambios = new JPanel();
        pnlCambios.setLayout(new BoxLayout(pnlCambios, BoxLayout.Y_AXIS));

        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setPreferredSize(new Dimension(pnlDatosProceso.getWidth(), 50)); 
        pnlDatosProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlDatosProceso.setLayout(null);

        JLabel lblTemperatura = new JLabel("Temperaturas");
        lblTemperatura.setBounds(10, 10, 100, 25);
        pnlDatosProceso.add(lblTemperatura);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(120, 10, 100, 25);
        pnlDatosProceso.add(dccDesde);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(250, 10, 100, 25);
        pnlDatosProceso.add(dccHasta);

        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel();
        pnlTemperaturasExtremas = new JPanel();

        tpTemperaturasCiudad = new JTabbedPane();
        tpTemperaturasCiudad.addTab("Gráfica", spGrafica);
        tpTemperaturasCiudad.addTab("Promedios", pnlEstadisticas);
        tpTemperaturasCiudad.addTab("Temperaturas extremas", pnlTemperaturasExtremas);

        
        pnlCambios.add(pnlDatosProceso);
        pnlCambios.add(tpTemperaturasCiudad);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlCambios, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        String nombreArchivo = System.getProperty("user.dir") + "/src/datos/Temperaturas.csv";
        datos = TemperaturaCiudadServicio.getDatos(nombreArchivo);

    }

    private void btnCalcularPromediosTemperaturasClick() {

        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        tpTemperaturasCiudad.setSelectedIndex(1);

        pnlEstadisticas.removeAll();
        pnlEstadisticas.setLayout(new GridBagLayout());

        String titulo = "<html><div style='text-align:center;'><b>Promedio de temperaturas para cada ciudad<br>entre: "
                + desde + " hasta " + hasta + "</b></div></html>";
        JLabel label = new JLabel(titulo);
        GridBagConstraints gbcTitulo = new GridBagConstraints();
        gbcTitulo.gridx = 0;
        gbcTitulo.gridy = 0;
        gbcTitulo.gridwidth = 2;
        pnlEstadisticas.add(label, gbcTitulo);

        int fila = 1;
        var promedios = TemperaturaCiudadServicio.getPromediosTemperaturas(desde, hasta, datos);
        for (var promedio : promedios.entrySet()) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = fila;
            pnlEstadisticas.add(new JLabel(promedio.getKey()), gbc);
            gbc.gridx = 1;
            pnlEstadisticas.add(new JLabel(String.format("%.2f", promedio.getValue())), gbc);
            fila++;
        }
        pnlEstadisticas.revalidate();
    }

    private void btnGraficarClick() {

        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    
        tpTemperaturasCiudad.setSelectedIndex(0); 
    
        SwingUtilities.invokeLater(() -> {
            
            Map<String, Double> promedios = TemperaturaCiudadServicio.getPromediosTemperaturas(desde, hasta, datos);
    
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            promedios.forEach((ciudad, promedio) -> {
                dataset.addValue(promedio, "Promedio", ciudad);
            });
            
            JFreeChart chart = ChartFactory.createBarChart(
                    "Promedio de Temperatura por Ciudad desde  " + desde + "  hasta  " + hasta,
                    "Ciudad",
                    "Promedio (°C)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);

                    
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
    
            
            pnlGrafica.removeAll();
            pnlGrafica.setLayout(new BorderLayout());
            pnlGrafica.add(chartPanel, BorderLayout.CENTER);
            pnlGrafica.revalidate();
            pnlGrafica.repaint();
        });
    }

    private void btnCalcularCiudadesTemperaturasExtremasClick() {

        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        
        tpTemperaturasCiudad.setSelectedIndex(2);

        pnlTemperaturasExtremas.removeAll();
        pnlTemperaturasExtremas.setLayout(new GridBagLayout());
        var temperaturas = TemperaturaCiudadServicio.getTemperaturasExtremas(desde, datos);
        int fila = 0;
        for (var temperatura : temperaturas.entrySet()) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = fila;
            pnlTemperaturasExtremas.add(new JLabel(temperatura.getKey()), gbc);
            gbc.gridx = 1;
            pnlTemperaturasExtremas.add(new JLabel(temperatura.getValue()), gbc);
            fila++;
        }
        pnlTemperaturasExtremas.revalidate();
    }

}
