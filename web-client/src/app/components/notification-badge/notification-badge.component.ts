import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-notification-badge',
  standalone: true,
  templateUrl: './notification-badge.component.html',
  styleUrls: ['./notification-badge.component.css']
})
export class NotificationBadgeComponent {
  @Input() connected = false;
  @Input() count = 0;
}
