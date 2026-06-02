import { describe, expect, test } from 'vitest';
import { getName, getPersonId } from '../../utils/fhirUtils';
import type { FhirPersonResource } from '../../types';

describe('getName', () => {
  const base: Omit<FhirPersonResource, 'name'> = {
    resourceType: 'Patient',
    id: '1',
    birthDate: '1990-01-01',
    gender: 'unknown',
  };

  test('returns empty string when names array is empty', () => {
    const person = { ...base, name: [] } as FhirPersonResource;
    expect(getName(person)).toBe('');
  });

  test('returns given and family (string)', () => {
    const person: FhirPersonResource = {
      ...base,
      name: [{ given: ['Jane'], family: 'Doe' }],
    };
    expect(getName(person)).toBe('Jane Doe');
  });

  test('includes prefix and suffix', () => {
    const person: FhirPersonResource = {
      ...base,
      name: [{ prefix: ['Dr.'], given: ['Jane'], family: 'Doe', suffix: ['PhD'] }],
    };
    expect(getName(person)).toBe('Dr. Jane Doe PhD');
  });

  test('handles family as an array', () => {
    const person: FhirPersonResource = {
      ...base,
      name: [{ given: ['Jane'], family: ['Doe', 'Smith'] }],
    };
    expect(getName(person)).toBe('Jane Doe Smith');
  });

  test('handles multiple given names', () => {
    const person: FhirPersonResource = {
      ...base,
      name: [{ given: ['Mary', 'Ann'], family: 'Jones' }],
    };
    expect(getName(person)).toBe('Mary Ann Jones');
  });

  test('prefers the name with use=official when multiple names exist', () => {
    const person: FhirPersonResource = {
      ...base,
      name: [
        { use: 'nickname', given: ['Johnny'], family: 'Cash' },
        { use: 'official', given: ['John'], family: 'Cash' },
      ],
    };
    expect(getName(person)).toBe('John Cash');
  });

  test('prefers the name with a later period.end when both have one', () => {
    const person: FhirPersonResource = {
      ...base,
      name: [
        { given: ['Old'], family: 'Name', period: { end: '2000-01-01' } },
        { given: ['New'], family: 'Name', period: { end: '2020-01-01' } },
      ],
    };
    expect(getName(person)).toBe('New Name');
  });
});

describe('getPersonId', () => {
  const baseName = [{ given: ['Jane'], family: 'Doe' }];

  test('returns qualified id for a Patient resource', () => {
    const patient: FhirPersonResource = {
      resourceType: 'Patient',
      id: 'patient-123',
      name: baseName,
      birthDate: '1990-01-01',
      gender: 'female',
    };
    expect(getPersonId(patient)).toBe('Patient/patient-123');
  });

  test('returns qualified id for a Practitioner resource', () => {
    const practitioner: FhirPersonResource = {
      resourceType: 'Practitioner',
      id: 'practitioner-456',
      name: baseName,
      birthDate: '1975-06-15',
      gender: 'male',
    };
    expect(getPersonId(practitioner)).toBe('Practitioner/practitioner-456');
  });

  test('uses the resource id verbatim', () => {
    const patient: FhirPersonResource = {
      resourceType: 'Patient',
      id: 'abc-def-789',
      name: baseName,
      birthDate: '2000-03-20',
      gender: 'unknown',
    };
    expect(getPersonId(patient)).toBe('Patient/abc-def-789');
  });
});
