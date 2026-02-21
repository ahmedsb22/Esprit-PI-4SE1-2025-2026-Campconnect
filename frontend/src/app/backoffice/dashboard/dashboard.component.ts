import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

declare var Chart: any;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit {
  stats = {
    totalSites: 0,
    totalUsers: 0,
    totalOrders: 0,
    revenue: 0
  };

  private apiUrl = `${environment.apiUrl}/analytics`;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadDashboardStats();
  }

  ngAfterViewInit() {
    // Initialize backoffice scripts
    setTimeout(() => {
      this.initBackofficeScripts();
      this.initCharts();
    }, 500);
  }

  loadDashboardStats() {
    this.http.get<any>(`${this.apiUrl}/dashboard`).subscribe({
      next: (stats) => {
        this.stats.totalSites = stats.totalSites ?? 0;
        this.stats.totalUsers = stats.totalUsers ?? 0;
        this.stats.totalOrders = (stats.totalOrders ?? 0) + (stats.totalBookings ?? 0);
        this.stats.revenue = stats.revenueStats?.totalRevenue ?? 0;
        this.initCharts(stats);
      },
      error: (err) => console.error('Failed to load dashboard stats', err)
    });
  }

  initCharts(apiStats?: any) {
    const revenueStats = apiStats?.revenueStats || {};
    const bookingStats = apiStats?.bookingStats || {};

    const areaCtx = document.getElementById('myAreaChart') as HTMLCanvasElement;
    if (areaCtx && typeof Chart !== 'undefined') {
      const monthlyRevenue = revenueStats.monthlyRevenue || [0, 800, 1200, 900, 1600, 2100, 1800, 2200, 2500, 3000, 2800, 3500];
      new Chart(areaCtx, {
        type: 'line',
        data: {
          labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
          datasets: [{
            label: 'Revenue',
            lineTension: 0.3,
            backgroundColor: 'rgba(78, 115, 223, 0.05)',
            borderColor: 'rgba(78, 115, 223, 1)',
            pointRadius: 3,
            pointBackgroundColor: 'rgba(78, 115, 223, 1)',
            pointBorderColor: 'rgba(78, 115, 223, 1)',
            pointHoverRadius: 3,
            pointHoverBackgroundColor: 'rgba(78, 115, 223, 1)',
            pointHoverBorderColor: 'rgba(78, 115, 223, 1)',
            pointHitRadius: 10,
            pointBorderWidth: 2,
            data: monthlyRevenue,
          }],
        },
        options: {
          maintainAspectRatio: false,
          layout: {
            padding: {
              left: 10,
              right: 25,
              top: 25,
              bottom: 0
            }
          },
          scales: {
            xAxes: [{
              gridLines: {
                display: false,
                drawBorder: false
              },
              ticks: {
                maxTicksLimit: 7
              }
            }],
            yAxes: [{
              ticks: {
                maxTicksLimit: 5,
                padding: 10,
                callback: function(value: any) {
                  return 'TND ' + value.toLocaleString();
                }
              },
              gridLines: {
                color: 'rgb(234, 236, 244)',
                zeroLineColor: 'rgb(234, 236, 244)',
                drawBorder: false,
                borderDash: [2],
                zeroLineBorderDash: [2]
              }
            }],
          },
          legend: {
            display: false
          },
          tooltips: {
            backgroundColor: 'rgb(255,255,255)',
            bodyFontColor: '#858796',
            titleMarginBottom: 10,
            titleFontColor: '#6e707e',
            titleFontSize: 14,
            borderColor: '#dddfeb',
            borderWidth: 1,
            xPadding: 15,
            yPadding: 15,
            displayColors: false,
            intersect: false,
            mode: 'index',
            caretPadding: 10,
            callbacks: {
              label: function(tooltipItem: any, chart: any) {
                var datasetLabel = chart.datasets[tooltipItem.datasetIndex].label || '';
                return datasetLabel + ': TND ' + tooltipItem.yLabel.toLocaleString();
              }
            }
          }
        }
      });
    }

    const pieCtx = document.getElementById('myPieChart') as HTMLCanvasElement;
    if (pieCtx && typeof Chart !== 'undefined') {
      const confirmed = bookingStats.confirmedBookings ?? 0;
      const pending = bookingStats.pendingBookings ?? 0;
      const cancelled = bookingStats.cancelledBookings ?? 0;
      new Chart(pieCtx, {
        type: 'doughnut',
        data: {
          labels: ['Confirmed', 'Pending', 'Cancelled'],
          datasets: [{
            data: [confirmed, pending, cancelled],
            backgroundColor: ['#4e73df', '#f6c23e', '#e74a3b'],
            hoverBackgroundColor: ['#2e59d9', '#dda20a', '#be2617'],
            hoverBorderColor: 'rgba(234, 236, 244, 1)',
          }],
        },
        options: {
          maintainAspectRatio: false,
          tooltips: {
            backgroundColor: 'rgb(255,255,255)',
            bodyFontColor: '#858796',
            borderColor: '#dddfeb',
            borderWidth: 1,
            xPadding: 15,
            yPadding: 15,
            displayColors: false,
            caretPadding: 10,
          },
          legend: {
            display: false
          },
          cutoutPercentage: 80,
        },
      });
    }
  }

  initBackofficeScripts() {
    // Initialize sidebar toggle
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarToggleTop = document.getElementById('sidebarToggleTop');
    const body = document.body;

    if (sidebarToggle) {
      sidebarToggle.addEventListener('click', () => {
        body.classList.toggle('sidebar-toggled');
        document.querySelector('.sidebar')?.classList.toggle('toggled');
      });
    }

    if (sidebarToggleTop) {
      sidebarToggleTop.addEventListener('click', () => {
        body.classList.toggle('sidebar-toggled');
        document.querySelector('.sidebar')?.classList.toggle('toggled');
      });
    }

    // Initialize scroll to top
    const scrollTop = document.querySelector('.scroll-to-top');
    if (scrollTop) {
      window.addEventListener('scroll', () => {
        if (window.scrollY > 100) {
          scrollTop.classList.add('active');
        } else {
          scrollTop.classList.remove('active');
        }
      });
      scrollTop.addEventListener('click', (e) => {
        e.preventDefault();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      });
    }
  }
}
