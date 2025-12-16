import { Component, input, OnInit, ViewChild, ElementRef, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Booking } from '../../../core/models/booking.model';
import * as QRCode from 'qrcode';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { AlertService } from '../../../core/services/alert.service';

@Component({
  selector: 'app-e-ticket',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './e-ticket.component.html',
  styleUrls: ['./e-ticket.component.css']
})
export class ETicketComponent implements OnInit {
  booking = input.required<Booking>();
  @ViewChild('ticketContent', { static: false }) ticketContent!: ElementRef;
  
  qrCodeDataUrl: string = '';
  private alertService = inject(AlertService);
  private cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.generateQRCode();
  }

  async generateQRCode(): Promise<void> {
    const booking = this.booking();
    const ticketInfo = `TICKET:${booking.ticketNumber}|MOVIE:${booking.movieTitle}|THEATER:${booking.theaterName}|TIME:${this.formatDateTime(booking.showtime)}|SEATS:${this.getSeatDisplay()}|AMOUNT:${this.formatCurrency(booking.totalAmount)}|ID:${booking.id}`;
    
    try {
      const url = await QRCode.toDataURL(ticketInfo, {
        width: 250,
        margin: 2,
        errorCorrectionLevel: 'H',
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        }
      });
      this.qrCodeDataUrl = url;
      this.cdr.detectChanges();
    } catch (error) {
      console.error('QR Code generation failed:', error);
      try {
        const fallbackUrl = await QRCode.toDataURL(booking.id, {
          width: 250,
          margin: 2,
          errorCorrectionLevel: 'M'
        });
        this.qrCodeDataUrl = fallbackUrl;
        this.cdr.detectChanges();
      } catch (fallbackError) {
        console.error('Fallback QR Code generation failed:', fallbackError);
        this.qrCodeDataUrl = '';
        this.cdr.detectChanges();
      }
    }
  }

  getSeatDisplay(): string {
    const booking = this.booking();
    if (booking.seatLabels && booking.seatLabels.length > 0) {
      return booking.seatLabels.join(', ');
    }
    return booking.seats?.join(', ') || 'N/A';
  }

  formatDateTime(date: string | Date): string {
    return new Date(date).toLocaleString('en-IN', {
      weekday: 'short',
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  }

  formatDate(date: string | Date): string {
    return new Date(date).toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  }

  async downloadPDF(): Promise<void> {
    try {
      this.alertService.info('Generating PDF...');
      const booking = this.booking();
      
      const element = this.ticketContent.nativeElement;
      
      const canvas = await html2canvas(element, {
        scale: 2,
        useCORS: true,
        allowTaint: true,
        logging: false,
        backgroundColor: '#f5f7fa',
        scrollY: 0,
        scrollX: 0,
        onclone: (clonedDoc) => {
          const qrSection = clonedDoc.querySelector('.qr-section');
          if (qrSection) {
            (qrSection as HTMLElement).style.display = 'none';
          }
        }
      });

      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = 210;
      const pageHeight = 297;
      const margin = 10;
      
      const imgWidth = pageWidth - (2 * margin);
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      
      const imgData = canvas.toDataURL('image/png', 1.0);
      
      if (imgHeight <= pageHeight - (2 * margin)) {
        pdf.addImage(imgData, 'PNG', margin, margin, imgWidth, imgHeight);
      } else {
        const scale = (pageHeight - (2 * margin)) / imgHeight;
        const scaledWidth = imgWidth * scale;
        const scaledHeight = pageHeight - (2 * margin);
        const xOffset = margin + (imgWidth - scaledWidth) / 2;
        pdf.addImage(imgData, 'PNG', xOffset, margin, scaledWidth, scaledHeight);
      }

      pdf.save(`RevTicket_${booking.ticketNumber || booking.id}.pdf`);
      this.alertService.success('Ticket downloaded successfully!');
    } catch (error) {
      console.error('PDF generation failed:', error);
      this.alertService.error('Failed to download PDF. Please try again.');
    }
  }

  async shareTicket(): Promise<void> {
    const booking = this.booking();
    const shareData = {
      title: `Movie Ticket - ${booking.movieTitle}`,
      text: `🎬 ${booking.movieTitle}\n🏢 ${booking.theaterName}\n📅 ${this.formatDateTime(booking.showtime)}\n💺 ${this.getSeatDisplay()}\n🎫 Ticket: ${booking.ticketNumber}`,
      url: window.location.href
    };

    if (navigator.share && navigator.canShare(shareData)) {
      try {
        await navigator.share(shareData);
        this.alertService.success('Ticket shared successfully!');
      } catch (error) {
        if ((error as Error).name !== 'AbortError') {
          this.copyToClipboard();
        }
      }
    } else {
      this.copyToClipboard();
    }
  }

  private copyToClipboard(): void {
    const booking = this.booking();
    const text = `🎬 ${booking.movieTitle}\n🏢 ${booking.theaterName}\n📅 ${this.formatDateTime(booking.showtime)}\n💺 ${this.getSeatDisplay()}\n🎫 ${booking.ticketNumber}`;
    
    navigator.clipboard.writeText(text).then(() => {
      this.alertService.success('Ticket details copied to clipboard!');
    }).catch(() => {
      this.alertService.error('Failed to copy ticket details.');
    });
  }

}
