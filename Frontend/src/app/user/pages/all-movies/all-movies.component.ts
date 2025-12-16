import { Component, OnInit, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MovieService } from '../../../core/services/movie.service';
import { AlertService } from '../../../core/services/alert.service';
import { LocationService } from '../../../core/services/location.service';
import { Movie } from '../../../core/models/movie.model';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';

@Component({
  selector: 'app-all-movies',
  standalone: true,
  imports: [CommonModule, LoaderComponent],
  templateUrl: './all-movies.component.html',
  styleUrls: ['./all-movies.component.css']
})
export class AllMoviesComponent implements OnInit {
  private movieService = inject(MovieService);
  private alertService = inject(AlertService);
  private router = inject(Router);
  private locationService = inject(LocationService);

  movies = signal<Movie[]>([]);
  loading = signal(true);

  constructor() {
    effect(() => {
      const city = this.locationService.selectedCity();
      this.loadMovies();
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {
    window.scrollTo(0, 0);
    this.loadMovies();
  }

  loadMovies(): void {
    this.loading.set(true);
    const city = this.locationService.selectedCity();
    this.movieService.getMovies(city || undefined).subscribe({
      next: (movies) => {
        const sortedMovies = movies
          .filter(m => m.isActive)
          .sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt).getTime() : new Date(a.releaseDate).getTime();
            const dateB = b.createdAt ? new Date(b.createdAt).getTime() : new Date(b.releaseDate).getTime();
            return dateB - dateA;
          });
        this.movies.set(sortedMovies);
        this.loading.set(false);
      },
      error: () => {
        this.alertService.error('Failed to load movies');
        this.loading.set(false);
      }
    });
  }

  viewDetails(movieId: string): void {
    const movie = this.movies().find(m => m.id === movieId);
    const slug = movie ? this.createSlug(movie.title) : movieId;
    this.router.navigate(['/user/movie-details', slug]);
  }

  viewShowtimes(event: Event, movieId: string): void {
    event.stopPropagation();
    const movie = this.movies().find(m => m.id === movieId);
    const slug = movie ? this.createSlug(movie.title) : movieId;
    this.router.navigate(['/user/showtimes', slug]);
  }

  bookNow(event: Event, movieId: string): void {
    event.stopPropagation();
    const movie = this.movies().find(m => m.id === movieId);
    const slug = movie ? this.createSlug(movie.title) : movieId;
    this.router.navigate(['/user/showtimes', slug]);
  }
  
  private createSlug(title: string): string {
    return title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
  }
}
