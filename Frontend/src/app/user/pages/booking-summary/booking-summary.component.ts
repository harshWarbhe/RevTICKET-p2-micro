import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { BookingDraft, BookingCostBreakdown } from '../../../core/models/booking.model';
import { BookingService } from '../../../core/services/booking.service';

@Component({
  selector: 'app-booking-summary',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './booking-summary.component.html',
  styleUrls: ['./booking-summary.component.css']
})
export class BookingSummaryComponent implements OnInit {
  bookingDraft = signal<BookingDraft | null>(null);
  costBreakdown = signal<BookingCostBreakdown | undefined>(undefined);

  private bookingService = inject(BookingService);
  private router = inject(Router);

  ngOnInit(): void {
    const draft = this.bookingService.getCurrentBooking();
    this.bookingDraft.set(draft);
    if (!draft) {
      this.router.navigate(['/user/home']);
      return;
    }

    this.costBreakdown.set(this.bookingService.calculateCostBreakdown(draft.totalAmount));
  }

  proceedToPayment(): void {
    this.router.navigate(['/user/payment']);
  }

  editSeats(): void {
    const draft = this.bookingDraft();
    if (draft) {
      this.router.navigate(['/user/booking', draft.showtimeId]);
    }
  }
}