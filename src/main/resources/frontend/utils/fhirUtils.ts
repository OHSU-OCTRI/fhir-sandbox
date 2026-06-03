import dayjs from 'dayjs';
import type { FhirHumanName, FhirPersonResource } from '../types';

/**
 * Converts a FHIR resource's array of FhirHumanName objects to a name string.
 *
 * @param person a FHIR resource with a names attribute containing FhirHumanName objects
 * @returns name string
 */
export function getName(person: FhirPersonResource): string {
  const names = person.name;
  if (!names?.length) {
    return '';
  }

  const sorted = [...names];
  if (sorted.length > 1) {
    sorted.sort((a: FhirHumanName, b: FhirHumanName) => {
      let score = 0;
      if (a.period?.end && b.period?.end) {
        score = dayjs(a.period.end).valueOf() - dayjs(b.period.end).valueOf();
      }
      if (a.use === 'official') {
        score += 1;
      }
      if (b.use === 'official') {
        score -= 1;
      }
      return score;
    });
  }

  const name = sorted[sorted.length - 1];
  const out: string[] = [];

  if (Array.isArray(name.prefix)) {
    out.push(name.prefix.join(' '));
  }

  if (Array.isArray(name.given)) {
    out.push(name.given.join(' '));
  }

  if (Array.isArray(name.family)) {
    out.push(name.family.join(' '));
  } else if (name.family) {
    out.push(name.family);
  }

  if (Array.isArray(name.suffix)) {
    out.push(name.suffix.join(' '));
  }
  return out.join(' ');
}

/**
 * Returns a qualified FHIR ID for use as a fhirUser claim for the given FHIR resource
 * (in this context a patient or practitioner).
 *
 * @param resource a FHIR person resource
 * @returns a qualified FHIR ID suitable for the fhirUser claim
 */
export function getPersonId(resource: FhirPersonResource) {
  return `${resource.resourceType}/${resource.id}`;
}
